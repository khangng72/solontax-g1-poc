package solontax.g1.management.core.domain.port;

import org.springframework.data.domain.Page;
import solontax.g1.management.core.common.dto.PersonQueryParams;
import solontax.g1.management.core.domain.model.Person;

import java.util.Optional;
import java.util.UUID;

public interface PersonRepositoryPort {
    Person save(Person person);

    Optional<Person> findById(UUID id);

    Optional<Person> findByTaxNumber(Long taxNumber);

    Page<Person> search(PersonQueryParams personQueryParams);
}
