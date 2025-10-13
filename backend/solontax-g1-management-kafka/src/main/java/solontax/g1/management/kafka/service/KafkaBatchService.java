package solontax.g1.management.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.stereotype.Service;
import solontax.g1.management.core.common.dto.KafkaBatchResponse;
import solontax.g1.management.kafka.entity.KafkaOffset;
import solontax.g1.management.kafka.entity.id.KafkaOffsetId;

import java.time.Duration;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaBatchService {
    private final KafkaOffsetService kafkaOffsetService;
    @Value("${spring.kafka.bootstrap-servers}")
    private String kafkaServer;

    public KafkaBatchResponse pollEvents(String topic, int maxEvents) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "solontax-g1-batch-group");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxEvents);

        KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(props);

        List<TopicPartition> partitions = consumer.partitionsFor(topic)
                .stream()
                .map(p -> new TopicPartition(topic, p.partition()))
                .toList();
        consumer.assign(partitions);

        for (TopicPartition tp : partitions) {
            KafkaOffsetId id = KafkaOffsetId.builder()
                    .topic(topic)
                    .partitionId(tp.partition())
                    .build();
            KafkaOffset lastOffsetRecord = kafkaOffsetService.findById(id).orElse(null);
            long lastOffset = lastOffsetRecord != null ? lastOffsetRecord.getLastOffset() : -1;
            long startOffset = lastOffset + 1;
            consumer.seek(tp, startOffset);
            log.info("Seeking partition {} to offset {}", tp.partition(), startOffset);
        }

        ConsumerRecords<String, Object> consumerRecords = consumer.poll(Duration.ofSeconds(1));
        log.info("Polled {} records", consumerRecords.count());

        List<Object> batch = new ArrayList<>();
        Map<Integer, Long> partitionOffsets = new HashMap<>();

        for (ConsumerRecord<String, Object> consumerRecord : consumerRecords) {
            batch.add(consumerRecord.value());
            partitionOffsets.put(
                    consumerRecord.partition(),
                    Math.max(partitionOffsets.getOrDefault(
                                    consumerRecord.partition(), -1L),
                            consumerRecord.offset())
            );
        }

        if (!batch.isEmpty()) {
            for (TopicPartition tp : partitions) {
                KafkaOffset updatedKafkaOffset = KafkaOffset.builder()
                        .topic(topic)
                        .partitionId(tp.partition())
                        .lastOffset(partitionOffsets.getOrDefault(tp.partition(), -1L))
                        .build();
                kafkaOffsetService.save(updatedKafkaOffset);
            }
        }

        Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
        long remaining = 0;
        for (TopicPartition tp : partitions) {
            log.info("tp:{}, position:{}, end:{}", tp, consumer.position(tp), endOffsets.get(tp));
            remaining += endOffsets.get(tp) - consumer.position(tp);
        }

        boolean hasMore = remaining > 0;
        log.info("HasMore: {}", hasMore);
        log.info("remaining: {}", remaining);

        return KafkaBatchResponse.builder()
                .events(batch)
                .messagesLeft(remaining)
                .hasMore(hasMore)
                .build();
    }
}
