package solontax.g1.management.kafka.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import solontax.g1.management.kafka.entity.KafkaOffset;
import solontax.g1.management.kafka.entity.id.KafkaOffsetId;

@Repository
public interface KafkaOffsetRepository extends JpaRepository<KafkaOffset, KafkaOffsetId> {
}
