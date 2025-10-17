package solontax.g1.management.kafka.config.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.ExponentialBackOff;
import solontax.g1.management.core.constant.KafkaTopics;
import solontax.g1.management.kafka.constants.ConsumerGroups;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@Slf4j
public class SingleConsumerConfig extends GenericConsumerConfig {

    private DeadLetterPublishingRecoverer createRecover(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (consumerRecord, exception) -> {
                    if (consumerRecord.topic().endsWith(".DLT")) {
                        log.error("Failed to resolved {}, send to parking-lot", consumerRecord.partition());
                        return new TopicPartition(KafkaTopics.PARKING_LOT, consumerRecord.partition());
                    }

                    String dltTopicName = consumerRecord.topic() + ".DLT";
                    log.error("Sending message to {} due to failure: partition={}, offset={}, error={}",
                            dltTopicName,
                            consumerRecord.partition(),
                            consumerRecord.offset(),
                            exception.getMessage()
                    );
                    return new TopicPartition(dltTopicName, consumerRecord.partition());
                }
        );
    }

    private DefaultErrorHandler createErrorHandler(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        ExponentialBackOff backOff = new ExponentialBackOff();
        backOff.setInitialInterval(retryInitialInterval);
        backOff.setMultiplier(retryMultiplier);
        backOff.setMaxAttempts(maxRetryAttempt);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                createRecover(kafkaTemplate),
                backOff
        );

        errorHandler.setRetryListeners(
                (consumerRecord, ex, deliveryAttempt)
                        -> log.warn("Retry attempt {} for record [topic={}, offset={}]: {}",
                        deliveryAttempt, consumerRecord.topic(), consumerRecord.offset(), ex.getMessage()));

        return errorHandler;
    }

    private ConsumerFactory<String, Object> createConsumerFactory() {
        JsonDeserializer<Object> deserializer = new JsonDeserializer<>(Object.class);

        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeHeaders(true);
        deserializer.setTypeMapper(typeMapper());

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, ConsumerGroups.SINGLE_DEFAULT_GROUP);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            KafkaTemplate<String, Object> kafkaTemplate
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(createConsumerFactory());
        factory.setCommonErrorHandler(createErrorHandler(kafkaTemplate));

        return factory;
    }
}
