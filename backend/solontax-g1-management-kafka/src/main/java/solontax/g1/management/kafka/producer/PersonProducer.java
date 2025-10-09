package solontax.g1.management.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import solontax.g1.management.core.common.dto.TaxCalculationDto;
import solontax.g1.management.core.constant.KafkaTopics;
import solontax.g1.management.core.constant.OperationType;
import solontax.g1.management.core.domain.model.Person;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void upsert(Person person) {
        kafkaTemplate.send(KafkaTopics.PERSON_CRUD_TOPIC, OperationType.UPSERT.toString(), person);
        log.info("Produced upsert message: {}", person.toString());
    }

    public void delete(UUID id) {
        kafkaTemplate.send(KafkaTopics.PERSON_CRUD_TOPIC, OperationType.DELETE.toString(), id.toString());
    }

    public void calculateTax(TaxCalculationDto taxCalculationDto) {
        kafkaTemplate.send(KafkaTopics.TAX_CALCULATION_TOPIC, OperationType.UPSERT.toString(), taxCalculationDto);
    }
}