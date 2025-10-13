package solontax.g1.management.kafka.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import solontax.g1.management.core.common.dto.TaxCalculationDto;
import solontax.g1.management.core.constant.KafkaTopics;
import solontax.g1.management.core.domain.model.Person;
import solontax.g1.management.core.domain.port.PersonRepositoryPort;
import solontax.g1.management.core.exception.CommonException;
import solontax.g1.management.kafka.config.utils.Utils;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchPersonConsumer {
    private final PersonRepositoryPort personRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

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

    private void sendToDeadLetter(
            ConsumerRecord<String, Object> consumerRecord,
            Exception exception) {
        String dltTopic = consumerRecord.topic() + ".DLT";
        log.warn("Sending record with key {} to DLT: {}", consumerRecord.key(), exception.getMessage());
        kafkaTemplate.send(dltTopic, consumerRecord.key(), consumerRecord.value());
    }

    @KafkaListener(
            topics = KafkaTopics.TAX_CALCULATION_TOPIC_BATCH,
            groupId = "solontax-g1-batch-group",
            containerFactory = "batchKafkaListenerContainerFactory"

    )
    public void listenTaxCalculationBatchEvents(
            List<ConsumerRecord<String, Object>> consumerRecordList,
            Acknowledgment ack
    ) {
        log.info("Listen tax calculation batch with size: {}", consumerRecordList.size());
        for (ConsumerRecord<String, Object> consumerRecord : consumerRecordList) {
            try {
                Utils.generateRandomFailure("Intended error", 0.5);
                TaxCalculationDto taxCalculationDto = (TaxCalculationDto) consumerRecord.value();
                processUpdateTaxDebt(taxCalculationDto);
            } catch (Exception exception) {
                sendToDeadLetter(consumerRecord, exception);
            }
        }

        ack.acknowledge();
    }

    @KafkaListener(
            topics = KafkaTopics.TAX_CALCULATION_TOPIC_BATCH + ".DLT",
            groupId = "solontax-g1-batch-dlt-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenTaxCalculationBatchDltEvents(ConsumerRecord<String, Object> consumerRecord) {
        log.info("Catch batch element failed: {}", consumerRecord.value());
        TaxCalculationDto taxCalculationDto = (TaxCalculationDto) consumerRecord.value();
        processUpdateTaxDebt(taxCalculationDto);
    }
}
