package solontax.g1.management.kafka.entity.id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KafkaOffsetId implements Serializable {
    private String topic;
    private int partitionId;
}
