package solontax.g1.management.core.application.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import solontax.g1.management.core.application.dto.PersonDto;
import solontax.g1.management.core.common.dto.PersonQueryParams;
import solontax.g1.management.core.domain.model.Person;
import solontax.g1.management.core.domain.port.PersonRepositoryPort;
import solontax.g1.management.core.exception.CommonException;

import java.time.LocalDate;
import java.util.Optional;

@Service
@AllArgsConstructor
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

    public PersonDto create(Person person) {
        try {
            Person savedPerson = personRepository.save(person);
            return toPersonDto(savedPerson);
        } catch (Exception ignored) {
            throw CommonException
                    .builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .message("Create person failed")
                    .build();
        }

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
}
