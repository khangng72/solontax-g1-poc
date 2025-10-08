package solontax.g1.management.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import solontax.g1.management.core.constant.KafkaTopics;
import solontax.g1.management.core.constant.OperationType;
import solontax.g1.management.core.domain.model.Person;
import solontax.g1.management.core.domain.port.PersonRepositoryPort;

import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor
public class PersonConsumer {
    private final PersonRepositoryPort personRepository;

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
}
