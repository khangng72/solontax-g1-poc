package solontax.g1.management.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import solontax.g1.management.core.common.dto.TaxCalculationDto;
import solontax.g1.management.core.constant.KafkaTopics;
import solontax.g1.management.core.domain.model.Person;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonProducer {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void upsert(Person person) {
        kafkaTemplate.send(KafkaTopics.UPSERT_PERSON_TOPIC, person.getTaxNumber().toString(), person);
        log.info("Produced person event: {}", person);
    }

    public void delete(UUID id) {
        kafkaTemplate.send(KafkaTopics.DELETE_PERSON_TOPIC, id.toString(), id);
        log.info("Produced delete person event: {}", id);
    }

    public void calculateTax(TaxCalculationDto taxCalculationDto) {
        kafkaTemplate.send(
                KafkaTopics.TAX_CALCULATION_TOPIC,
                taxCalculationDto.getTaxNumber().toString(),
                taxCalculationDto);
    }

    public void upsertForManualConsume(Person person) {
        kafkaTemplate.send(KafkaTopics.UPSERT_PERSON_BATCH_TOPIC, person.getTaxNumber().toString(), person);
        log.info("Produced Upsert person event for manual consumer");
    }

    public void calculateTaxInBatch(List<TaxCalculationDto> taxCalculationDtoList) {
        for (TaxCalculationDto taxCalculationDto : taxCalculationDtoList) {
            kafkaTemplate.send(
                    KafkaTopics.TAX_CALCULATION_TOPIC_BATCH,
                    taxCalculationDto.getTaxNumber().toString(),
                    taxCalculationDto
            );
        }
    }
}