package solontax.g1.management.adapter.inbound.http;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import solontax.g1.management.core.application.dto.PersonDto;
import solontax.g1.management.core.application.service.PersonService;
import solontax.g1.management.core.common.dto.PersonQueryParams;
import solontax.g1.management.core.domain.model.Person;
import solontax.g1.management.kafka.producer.PersonProducer;

@RestController
@RequestMapping("/v1/person")
@RequiredArgsConstructor
@Log4j2
public class PersonController {

    private final PersonService personService;
    private final PersonProducer producer;

    @PostMapping
    public ResponseEntity<PersonDto> create(@RequestBody Person person) {
        PersonDto savedPerson = personService.create(person);
        return new ResponseEntity<>(savedPerson, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<PersonDto>> search(@ModelAttribute PersonQueryParams personQueryParams) {
        Page<PersonDto> personDtoPage = personService.search(personQueryParams);
        return new ResponseEntity<>(personDtoPage, HttpStatus.OK);
    }

    @GetMapping("/by-tax-number/{taxNumber}")
    public ResponseEntity<PersonDto> findByTaxNumber(@PathVariable("taxNumber") Long taxNumber) {
        PersonDto foundPerson = personService.findByTaxNumber(taxNumber);
        return new ResponseEntity<>(foundPerson, HttpStatus.OK);
    }

    @PostMapping("/kafka")
    public String testKafka(@RequestBody Person person) {
        producer.sendMessage("person-events", person.getFirstName());
        return "kafka";
    }
}
