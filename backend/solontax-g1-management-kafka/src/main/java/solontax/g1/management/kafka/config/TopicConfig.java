package solontax.g1.management.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import solontax.g1.management.core.constant.KafkaTopics;

@Configuration
public class TopicConfig {

    @Bean
    public NewTopic createUpsertPersonBatchTopic() {
        return TopicBuilder.name(KafkaTopics.UPSERT_PERSON_BATCH_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
