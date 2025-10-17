package solontax.g1.management.core.application.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import solontax.g1.management.core.application.dto.PersonDto;
import solontax.g1.management.core.common.dto.PersonQueryParams;
import solontax.g1.management.core.common.dto.TaxCalculationDto;
import solontax.g1.management.core.domain.model.Person;
import solontax.g1.management.core.domain.port.PersonRepositoryPort;
import solontax.g1.management.core.exception.CommonException;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class PersonService {
    private final PersonRepositoryPort personRepository;

    private PersonDto toPersonDto(Person person) {
        Long age = person.getDateOfBirth() != null
                ? (long) (LocalDate.now().getYear() - person.getDateOfBirth().getYear())
                : null;

        return PersonDto.builder()
                .id(person.getId())
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .age(age)
                .taxNumber(person.getTaxNumber())
                .taxDebt(person.getTaxDebt())
                .build();
    }

    public PersonDto upsert(Person person) {
        if (person.getId() != null) {
            Person existedPerson = personRepository.findById(person.getId())
                    .orElseThrow(() -> new CommonException(
                            "Cannot upsert person with id " + person.getId(),
                            HttpStatus.INTERNAL_SERVER_ERROR
                    ));


            person.setTaxNumber(existedPerson.getTaxNumber());
        }

        Person savedPerson = personRepository.save(person);
        return toPersonDto(savedPerson);
    }

    public PersonDto findByTaxNumber(Long taxNumber) throws CommonException {
        Optional<Person> foundPerson = personRepository.findByTaxNumber(taxNumber);

        if (foundPerson.isPresent()) {
            return toPersonDto(foundPerson.get());
        }

        throw CommonException.builder()
                .message("Cannot found person with tax number: " + taxNumber)
                .status(HttpStatus.NOT_FOUND)
                .build();
    }

    public Page<PersonDto> search(PersonQueryParams personQueryParams) {
        Page<Person> personPage = personRepository.search(personQueryParams);

        return personPage.map(this::toPersonDto);
    }

    public void delete(UUID id) {
        personRepository.deleteById(id);
    }

    @Transactional
    public void processUpdateTaxDebt(TaxCalculationDto taxCalculationDto) {
        Optional<Person> person = personRepository.findByTaxNumber(taxCalculationDto.getTaxNumber());

        person.ifPresentOrElse(
                p -> {
                    Long previousTaxDebt = p.getTaxDebt();
                    p.setTaxDebt(previousTaxDebt + taxCalculationDto.getCalculatedTax());
                    Person savedPerson = personRepository.save(p);
                    log.info("[UPDATE tax successfully] Prev:{} Added:{} Final:{}",
                            previousTaxDebt,
                            taxCalculationDto.getCalculatedTax(),
                            savedPerson.getTaxDebt()
                    );
                },
                () -> {
                    throw new CommonException("update tax failed", HttpStatus.INTERNAL_SERVER_ERROR);
                }
        );
    }
}
