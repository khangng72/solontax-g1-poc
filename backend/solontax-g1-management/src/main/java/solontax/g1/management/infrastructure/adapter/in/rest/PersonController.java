package solontax.g1.management.infrastructure.adapter.in.rest;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solontax.g1.management.application.dto.PersonDto;
import solontax.g1.management.application.service.PersonService;
import solontax.g1.management.common.dto.PersonQueryParams;
import solontax.g1.management.domain.model.Person;

@RestController
@RequestMapping("/v1/person")
@AllArgsConstructor
@Log4j2
public class PersonController {

    private final PersonService personService;

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
}
