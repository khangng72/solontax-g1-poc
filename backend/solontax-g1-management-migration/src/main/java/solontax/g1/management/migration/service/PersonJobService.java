package solontax.g1.management.migration.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import solontax.g1.management.core.common.dto.KafkaBatchResponse;
import solontax.g1.management.core.domain.model.Person;
import solontax.g1.management.core.domain.port.PersonRepositoryPort;
import solontax.g1.management.migration.constant.DataGenerator;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PersonJobService {
    private final RestTemplate restTemplate;
    private final PersonRepositoryPort personRepository;

    @Value("${kafka.endpoint.domain}")
    private String domain;

    private List<Person> mapToPersonList(List<Object> objects) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        CollectionType listType = mapper.getTypeFactory()
                .constructCollectionType(List.class, Person.class);

        return mapper.convertValue(objects, listType);
    }

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

        if (response != null) {
            List<Person> personList = mapToPersonList(response.getEvents());
            for (Person p: personList) {
                personRepository.save(p);
            }
        }
    }
}
