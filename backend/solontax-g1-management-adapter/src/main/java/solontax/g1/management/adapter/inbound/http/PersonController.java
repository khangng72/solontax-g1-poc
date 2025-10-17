package solontax.g1.management.adapter.inbound.http;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solontax.g1.management.core.application.dto.PersonDto;
import solontax.g1.management.core.application.service.PersonService;
import solontax.g1.management.core.common.dto.KafkaBatchResponse;
import solontax.g1.management.core.common.dto.PersonQueryParams;
import solontax.g1.management.core.common.dto.TaxCalculationDto;
import solontax.g1.management.core.constant.KafkaTopics;
import solontax.g1.management.core.domain.model.Person;
import solontax.g1.management.kafka.producer.PersonProducer;
import solontax.g1.management.kafka.service.KafkaBatchService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/person")
@RequiredArgsConstructor
@Log4j2
public class PersonController {

    private final PersonService personService;
    private final PersonProducer producer;
    private final KafkaBatchService kafkaBatchService;

    @PostMapping
    public ResponseEntity<PersonDto> upsert(@RequestBody Person person) {
        PersonDto savedPerson = personService.upsert(person);
        return new ResponseEntity<>(savedPerson, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<PersonDto>> search(@ModelAttribute PersonQueryParams personQueryParams) {
        Page<PersonDto> personDtoPage = personService.search(personQueryParams);
        return new ResponseEntity<>(personDtoPage, HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> delete(@PathVariable("id") UUID id) {
        personService.delete(id);
        return ResponseEntity.ok("Deleted person with id = " + id);
    }

    @GetMapping("/by-tax-number/{taxNumber}")
    public ResponseEntity<PersonDto> findByTaxNumber(@PathVariable("taxNumber") Long taxNumber) {
        PersonDto foundPerson = personService.findByTaxNumber(taxNumber);
        return new ResponseEntity<>(foundPerson, HttpStatus.OK);
    }

    @PostMapping("/kafka")
    public ResponseEntity<String> sendUpsertEvent(@RequestBody Person person) {
        producer.upsert(person);
        return ResponseEntity.ok("Upsert person event sent");
    }

    @DeleteMapping("/kafka/{id}")
    public ResponseEntity<String> sendDeleteEvent(@PathVariable("id") UUID id) {
        producer.delete(id);
        return ResponseEntity.ok("Delete person event send");
    }

    @PostMapping("/kafka/tax")
    public ResponseEntity<String> sendTaxCalculationEvent(@RequestBody @Valid TaxCalculationDto taxCalculationDto) {
        producer.calculateTax(taxCalculationDto);
        return ResponseEntity.ok("Tax calculation event sent");
    }

    @PostMapping("/kafka/batch/tax")
    public ResponseEntity<String> sendTaxCalculationEvent(
            @RequestBody @Valid List<TaxCalculationDto> taxCalculationDtoList) {
        producer.calculateTaxInBatch(taxCalculationDtoList);
        return ResponseEntity.ok("Tax calculation in batch event sent");
    }

    @PostMapping("/kafka/batch/manual")
    public ResponseEntity<String> sendUpsertEventForManualConsume(@RequestBody @Valid Person person) {
        producer.upsertForManualConsume(person);
        return ResponseEntity.ok("Upsert person event is sent to upsert-person-batch");
    }

    @GetMapping("/kafka/batch/manual")
    public ResponseEntity<KafkaBatchResponse> consumeUpsertPersonBatchEvent() {
        KafkaBatchResponse response = kafkaBatchService.pollEvents(KafkaTopics.UPSERT_PERSON_BATCH_TOPIC, 3);
        return ResponseEntity.ok(response);
    }
}
