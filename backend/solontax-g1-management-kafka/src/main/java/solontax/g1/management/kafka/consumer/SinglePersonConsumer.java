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
import solontax.g1.management.kafka.constants.ConsumerGroups;

import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class SinglePersonConsumer {
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
            groupId = ConsumerGroups.SINGLE_UPSERT_PERSON_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenUpsertPersonEvents(ConsumerRecord<String, Object> consumerRecord) {
        log.info("Start performing [UPSERT] operation on entity person");
        try {
            Utils.generateRandomFailure("Intended Error", 0.9);
            Person person = (Person) consumerRecord.value();
            Person savePerson = personRepository.save(person);
            log.info("Upsert person with id = {} successfully", savePerson.getId());
        } catch (Exception e) {
            log.error("Upsert person: {} failed", consumerRecord.value());
            throw e;
        }
    }

    @KafkaListener(
            topics = KafkaTopics.UPSERT_PERSON_TOPIC + ".DLT",
            groupId = ConsumerGroups.SINGLE_UPSERT_PERSON_GROUP + ".DLT"
    )
    public void listenUpsertPersonDltEvents(ConsumerRecord<String, Object> consumerRecord) {
        try {
            Person person = (Person) consumerRecord.value();
            Person savePerson = personRepository.save(person);
            log.info("Resolve upsert person with id = {} successfully", savePerson.getId());
        } catch (Exception e) {
            log.error("Resolve upsert person: {} failed", consumerRecord.value());
            throw e;
        }
    }


    @KafkaListener(
            topics = KafkaTopics.DELETE_PERSON_TOPIC,
            groupId = ConsumerGroups.SINGLE_DELETE_PERSON_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenDeletePersonEvents(ConsumerRecord<String, Object> consumerRecord) {
        log.info("Start performing [DELETE] operation on person");
        try {
            Utils.generateRandomFailure("Intended error", 0.9);
            UUID deleteId = (UUID) consumerRecord.value();
            personRepository.deleteById(deleteId);
            log.info("Delete person with id = {} successfully", deleteId);
        } catch (Exception e) {
            log.error("Delete person with id = {} failed", consumerRecord.value());
            throw e;
        }
    }

    @KafkaListener(
            topics = KafkaTopics.DELETE_PERSON_TOPIC + ".DLT",
            groupId = ConsumerGroups.SINGLE_DELETE_PERSON_GROUP + ".DLT"
    )
    public void listenDeletePersonDltEvents(ConsumerRecord<String, Object> consumerRecord) {
        try {
            log.info("Try to resolve {}", consumerRecord.partition());
            UUID deleteId = (UUID) consumerRecord.value();
            personRepository.deleteById(deleteId);
        } catch (Exception e) {
            log.error("Failed to resolve: {}", consumerRecord.partition());
            throw e;
        }
    }


    @KafkaListener(
            topics = KafkaTopics.TAX_CALCULATION_TOPIC,
            groupId = ConsumerGroups.SINGLE_TAX_CALCULATION_GROUP,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenTaxCalculationEvents(ConsumerRecord<String, Object> consumedEvent) {
        log.info("Start updating tax debt: {}", consumedEvent.value());
        try {
            TaxCalculationDto taxCalculationDto = (TaxCalculationDto) consumedEvent.value();
            // This generator is used to cause fail randomly
            Utils.generateRandomFailure("Intended error", 0.9);
            processUpdateTaxDebt(taxCalculationDto);
        } catch (Exception e) {
            log.error("Error updating tax debt: {}", consumedEvent.value());
            throw e;
        }
    }

    @KafkaListener(
            topics = KafkaTopics.TAX_CALCULATION_TOPIC + ".DLT",
            groupId = ConsumerGroups.SINGLE_TAX_CALCULATION_GROUP + ".DLT",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenTaxCalculationDltEvents(ConsumerRecord<String, Object> consumedEvent) {
        log.info("Resolve updating tax debt dead letter: {}", consumedEvent.value());
        try {
            TaxCalculationDto taxCalculationDto = (TaxCalculationDto) consumedEvent.value();
            Utils.generateRandomFailure("Intended error: ", 0.2);
            processUpdateTaxDebt(taxCalculationDto);
        } catch (Exception e) {
            log.error("Fail to resolve updating tax debt dead letter: {}", consumedEvent.value());
            throw e;
        }
    }
}