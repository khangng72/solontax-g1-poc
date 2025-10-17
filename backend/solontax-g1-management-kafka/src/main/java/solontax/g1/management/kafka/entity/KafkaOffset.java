package solontax.g1.management.kafka.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.*;
import solontax.g1.management.kafka.entity.id.KafkaOffsetId;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "kafka_offsets")
@IdClass(KafkaOffsetId.class)
public class KafkaOffset {
    @Id
    private String topic;
    @Id
    private int partitionId;
    private long lastOffset;
}
