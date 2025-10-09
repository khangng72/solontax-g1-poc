package solontax.g1.management.kafka.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.RetriableException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;
import solontax.g1.management.kafka.config.utils.Utils;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Slf4j
public class KafkaBatchConsumerConfig {
    private static final Integer MAX_ATTEMPT = 3;
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    private static DefaultErrorHandler createBatchErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                ((consumerRecord, exception) -> new TopicPartition(
                        consumerRecord.topic() + ".DLT", consumerRecord.partition()
                ))
        );

        ExponentialBackOff backOff = new ExponentialBackOff(3000L, 1);
        backOff.setMaxAttempts(MAX_ATTEMPT);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addRetryableExceptions(RetriableException.class);

        return handler;
    }

    @Bean
    public ConsumerFactory<String, Object> batchConsumerFactory() {
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>(Object.class);
        deserializer.addTrustedPackages("*");
        deserializer.setTypeMapper(Utils.typeMapper());

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "solontax-g1-batch-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5);

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean(name = "kafkaBatchListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaBatchListenerContainerFactory(
            @Qualifier("batchConsumerFactory") ConsumerFactory<String, Object> batchConsumerFactory,
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(batchConsumerFactory);
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(createBatchErrorHandler(kafkaTemplate));

        return factory;
    }
}
