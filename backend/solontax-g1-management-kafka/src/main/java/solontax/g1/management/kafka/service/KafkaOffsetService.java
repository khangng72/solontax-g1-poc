package solontax.g1.management.kafka.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import solontax.g1.management.kafka.entity.KafkaOffset;
import solontax.g1.management.kafka.entity.id.KafkaOffsetId;
import solontax.g1.management.kafka.repository.KafkaOffsetRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class KafkaOffsetService {
    private final KafkaOffsetRepository kafkaOffsetRepository;

    public void save(KafkaOffset kafkaOffset) {
        kafkaOffsetRepository.save(kafkaOffset);
    }

    public Optional<KafkaOffset> findById(KafkaOffsetId id) {
        return kafkaOffsetRepository.findById(id);
    }
}
