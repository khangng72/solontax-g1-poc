package solontax.g1.management.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import solontax.g1.management.core.common.dto.TaxCalculationDto;
import solontax.g1.management.core.constant.KafkaTopics;
import solontax.g1.management.core.constant.OperationType;
import solontax.g1.management.core.domain.model.Person;
import solontax.g1.management.core.domain.port.PersonRepositoryPort;
import solontax.g1.management.core.exception.CommonException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class PersonConsumer {
    private final PersonRepositoryPort personRepository;

    private void processUpdateTaxDebt(TaxCalculationDto taxCalculationDto) {
        Optional<Person> person = personRepository.findByTaxNumber(taxCalculationDto.getTaxNumber());

        person.ifPresentOrElse(
                p -> {
                    p.setTaxDebt(p.getTaxDebt() + taxCalculationDto.getCalculatedTax());
                    personRepository.save(p);
                    log.info("Consumer consume message successfully");
                },
                () -> {
                    throw new CommonException("update tax failed", HttpStatus.INTERNAL_SERVER_ERROR);
                }
        );
    }

    @KafkaListener(
            topics = KafkaTopics.PERSON_CRUD_TOPIC,
            groupId = "solontax-g1-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenPersonCrudEvents(ConsumerRecord<String, Object> consumedEvent) {
        String operationTypeString = consumedEvent.key();
        OperationType operationType = OperationType.valueOf(operationTypeString);
        Object value = consumedEvent.value();

        if (operationType == OperationType.DELETE) {
            UUID id;
            if (value instanceof String stringValue) {
                try {
                    id = UUID.fromString(stringValue);
                    personRepository.deleteById(id);
                    return;
                } catch (IllegalArgumentException e) {
                    log.error("Invalid UUID format: {}", stringValue);
                    return;
                }
            }
        }

        Person person;
        if (value instanceof Person p) {
            person = p;
        } else {
            try {
                ObjectMapper mapper = new ObjectMapper();
                person = mapper.convertValue(value, Person.class);
            } catch (Exception e) {
                log.error("Failed to convert value to Person: {}", e.getMessage());
                return;
            }
        }

        if (operationType == OperationType.DELETE) {
            personRepository.deleteById(person.getId());
        } else {
            personRepository.save(person);
        }
    }

    @KafkaListener(
            topics = KafkaTopics.TAX_CALCULATION_TOPIC,
            groupId = "solontax-g1-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenTaxCalculationEvents(ConsumerRecord<String, Object> consumedEvent)
            throws CommonException {
        if (LocalDateTime.now().getSecond() % 2 == 0) {
            log.info("Virtually cause the consumer to fail: {}", LocalDateTime.now());
            throw new CommonException("Virtual failure of updating tax", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        TaxCalculationDto taxCalculationDto = (TaxCalculationDto) consumedEvent.value();
        processUpdateTaxDebt(taxCalculationDto);

    }

    @KafkaListener(
            topics = KafkaTopics.TAX_CALCULATION_TOPIC + ".DLT",
            groupId = "solontax-g1-group",
            containerFactory = "dltListenerContainerFactory"
    )
    public void listenTaxCalculationEventsDeadLetter(ConsumerRecord<String, Object> consumedEvent) {
        log.info("Dead letter queue try to consume: {}", consumedEvent.value());
        TaxCalculationDto taxCalculationDto = (TaxCalculationDto) consumedEvent.value();
        processUpdateTaxDebt(taxCalculationDto);
        log.info("Resolve dead event: {} successfully", consumedEvent);
    }
}
