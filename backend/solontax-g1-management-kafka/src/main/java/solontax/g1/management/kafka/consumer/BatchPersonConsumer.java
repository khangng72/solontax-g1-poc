package solontax.g1.management.kafka.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import solontax.g1.management.core.application.service.PersonService;
import solontax.g1.management.core.common.dto.TaxCalculationDto;
import solontax.g1.management.core.constant.KafkaTopics;
import solontax.g1.management.kafka.config.utils.Utils;
import solontax.g1.management.kafka.constants.ConsumerGroups;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BatchPersonConsumer {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final PersonService personService;


    private void sendToDeadLetter(
            ConsumerRecord<String, Object> consumerRecord,
            Exception exception) {
        String dltTopic = consumerRecord.topic() + ".DLT";
        log.warn("Sending record with key {} to DLT: {}", consumerRecord.key(), exception.getMessage());
        kafkaTemplate.send(dltTopic, consumerRecord.key(), consumerRecord.value());
    }

    @KafkaListener(
            topics = KafkaTopics.TAX_CALCULATION_TOPIC_BATCH,
            groupId = ConsumerGroups.BATCH_TAX_CALCULATION_GROUP,
            containerFactory = "batchKafkaListenerContainerFactory"

    )
    public void listenTaxCalculationBatchEvents(
            List<ConsumerRecord<String, Object>> consumerRecordList,
            Acknowledgment ack
    ) {
        log.info("Listen tax calculation batch with size: {}", consumerRecordList.size());
        for (ConsumerRecord<String, Object> consumerRecord : consumerRecordList) {
            try {
                Utils.generateRandomFailure("intended error", 0.8);
                TaxCalculationDto taxCalculationDto = (TaxCalculationDto) consumerRecord.value();
                personService.processUpdateTaxDebt(taxCalculationDto);
                ack.acknowledge();
            } catch (Exception exception) {
                ack.acknowledge();
                sendToDeadLetter(consumerRecord, exception);
            }
        }
    }

    @KafkaListener(
            topics = KafkaTopics.TAX_CALCULATION_TOPIC_BATCH + ".DLT",
            groupId = ConsumerGroups.BATCH_TAX_CALCULATION_GROUP + ".DLT",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void listenTaxCalculationBatchDltEvents(ConsumerRecord<String, Object> consumerRecord) {
        log.info("Catch batch element failed: {}", consumerRecord.value());
        TaxCalculationDto taxCalculationDto = (TaxCalculationDto) consumerRecord.value();
        personService.processUpdateTaxDebt(taxCalculationDto);
    }
}
