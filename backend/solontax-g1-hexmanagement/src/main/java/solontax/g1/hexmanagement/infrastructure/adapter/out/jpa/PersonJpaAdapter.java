package solontax.g1.hexmanagement.infrastructure.adapter.out.jpa;

import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import solontax.g1.hexmanagement.domain.model.Person;
import solontax.g1.hexmanagement.domain.port.PersonRepositoryPort;
import solontax.g1.hexmanagement.infrastructure.adapter.out.entity.PersonEntity;
import solontax.g1.hexmanagement.infrastructure.repository.PersonJpaRepository;

@Component
@AllArgsConstructor
public class PersonJpaAdapter implements PersonRepositoryPort {
    private final PersonJpaRepository repository;

    private PersonEntity toPersonEntity(Person person) {
        return PersonEntity
                .builder()
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .dateOfBirth(person.getDateOfBirth())
                .taxNumber(person.getTaxNumber())
                .build();
    }

    private Person toPerson(PersonEntity entity) {
        return Person
                .builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .dateOfBirth(entity.getDateOfBirth())
                .taxNumber(entity.getTaxNumber())
                .build();
    }

    @Override
    public Person save(Person person) {
        PersonEntity newPerson = toPersonEntity(person);
        PersonEntity savedPerson = repository.save(newPerson);
        return toPerson(savedPerson);
    }

    @Override
    public Optional<Person> findById(UUID id) {
        Optional<PersonEntity> foundedPerson = repository.findById(id);

        if (foundedPerson.isPresent()) {
            return foundedPerson.map(this::toPerson);
        }

        return Optional.empty();
    }

    @Override
    public Optional<Person> findByTaxNumber(Long taxNumber) {
        Optional<PersonEntity> foundPerson = repository.findByTaxNumber(taxNumber);

        if (foundPerson.isPresent()) {
            return foundPerson.map(this::toPerson);
        }

        return Optional.empty();
    }
}
