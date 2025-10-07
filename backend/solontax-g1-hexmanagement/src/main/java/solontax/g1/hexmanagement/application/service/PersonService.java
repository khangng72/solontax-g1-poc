package solontax.g1.hexmanagement.application.service;

import java.time.LocalDate;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import solontax.g1.hexmanagement.application.dto.PersonDto;
import solontax.g1.hexmanagement.domain.model.Person;
import solontax.g1.hexmanagement.domain.port.PersonRepositoryPort;
import solontax.g1.hexmanagement.exception.CommonException;

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
}
