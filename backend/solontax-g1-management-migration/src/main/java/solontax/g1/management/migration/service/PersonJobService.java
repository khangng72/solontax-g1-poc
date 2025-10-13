package solontax.g1.management.migration.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import solontax.g1.management.core.common.dto.KafkaBatchResponse;
import solontax.g1.management.core.domain.model.Person;
import solontax.g1.management.migration.constant.DataGenerator;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonJobService {
    private final RestTemplate restTemplate;
    @Value("${kafka.endpoint.domain}")
    private String domain;

    public void sendUpsertPersonEvents() {
        String endpoint = "/v1/person/kafka/batch/manual";
        Person person = Person.builder()
                .firstName(DataGenerator.randomFirstName())
                .lastName(DataGenerator.randomLastName())
                .dateOfBirth(DataGenerator.randomDateOfBirth())
                .taxNumber(DataGenerator.randomTaxNumber())
                .build();
        String url = domain + endpoint;
        String response = restTemplate.postForObject(url, person, String.class);
        log.info("Cron job send upsert person event, response: {}", response);
    }

    public void consumeUpsertPersonEvents() {
        String endpoint= "/v1/person/kafka/batch/manual";
        String url = domain + endpoint;
        KafkaBatchResponse response = restTemplate.getForObject(url, KafkaBatchResponse.class);
        log.info("Cron job consume upsert person event, response: {}", response);
    }
}
