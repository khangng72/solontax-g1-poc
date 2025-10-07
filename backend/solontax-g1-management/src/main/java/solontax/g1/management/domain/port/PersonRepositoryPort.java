package solontax.g1.management.domain.port;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import solontax.g1.management.common.dto.PersonQueryParams;
import solontax.g1.management.domain.model.Person;

public interface PersonRepositoryPort {
    Person save(Person person);

    Optional<Person> findById(UUID id);

    Optional<Person> findByTaxNumber(Long taxNumber);

    Page<Person> search(PersonQueryParams personQueryParams);
}
