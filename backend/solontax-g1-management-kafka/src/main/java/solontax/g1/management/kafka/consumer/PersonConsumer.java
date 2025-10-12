package solontax.g1.management.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import solontax.g1.management.core.common.dto.TaxCalculationDto;
import solontax.g1.management.core.constant.KafkaTopics;
import solontax.g1.management.core.domain.model.Person;
import solontax.g1.management.core.domain.port.PersonRepositoryPort;
import solontax.g1.management.core.exception.CommonException;
import solontax.g1.management.kafka.config.utils.Utils;

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
                    log.info("Process update taxDebt successfully");
                },
                () -> {
                    throw new CommonException("update tax failed", HttpStatus.INTERNAL_SERVER_ERROR);
                }
        );
    }

    @KafkaListener(
            topics = KafkaTopics.UPSERT_PERSON_TOPIC,
            groupId = "solontax-g1-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenUpsertPersonEvents(ConsumerRecord<String, Object> consumerRecord) {
        log.info("Start performing [UPSERT] operation on entity person");
        try {
            Person person = (Person) consumerRecord.value();
            Person savePerson = personRepository.save(person);
            log.info("Upsert person with id = {} successfully", savePerson.getId());
        } catch (Exception e) {
            log.error("Upsert person: {} failed", consumerRecord.value());
            throw e;
        }
    }

    @KafkaListener(
            topics = KafkaTopics.DELETE_PERSON_TOPIC,
            groupId = "solontax-g1-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenDeletePersonEvents(ConsumerRecord<String, Object> consumerRecord) {
        log.info("Start performing [DELETE] operation on person");
        try {
            UUID deleteId = (UUID) consumerRecord.value();
            personRepository.deleteById(deleteId);
            log.info("Delete person with id = {} successfully", deleteId);
        } catch (Exception e) {
            log.error("Delete person with id = {} failed", consumerRecord.value());
            throw e;
        }
    }


    @KafkaListener(
            topics = KafkaTopics.TAX_CALCULATION_TOPIC,
            groupId = "solontax-g1-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenTaxCalculationEvents(ConsumerRecord<String, Object> consumedEvent) {
        log.info("Start updating tax debt: {}", consumedEvent.value());
        try {
            TaxCalculationDto taxCalculationDto = (TaxCalculationDto) consumedEvent.value();
            Utils.generateRandomFailure("Intended error", 0.5);
            processUpdateTaxDebt(taxCalculationDto);
        } catch (Exception e) {
            log.error("Error updating tax debt: {}", consumedEvent.value());
            throw e;
        }
    }
}