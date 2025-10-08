package solontax.g1.management.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PersonConsumer {
    @KafkaListener(topics = "person-events", groupId = "solontax-g1")
    public void listen(String message) {
        log.info("Received message {}", message);
    }
}
