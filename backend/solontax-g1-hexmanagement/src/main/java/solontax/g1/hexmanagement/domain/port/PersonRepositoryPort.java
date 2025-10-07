package solontax.g1.hexmanagement.domain.port;

import java.util.Optional;
import java.util.UUID;
import solontax.g1.hexmanagement.domain.model.Person;

public interface PersonRepositoryPort {
    Person save(Person person);

    Optional<Person> findById(UUID id);

    Optional<Person> findByTaxNumber(Long taxNumber);
}
