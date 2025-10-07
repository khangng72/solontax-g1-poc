package solontax.g1.management.infrastructure.adapter.out.jpa;

import cz.jirutka.rsql.parser.RSQLParser;
import cz.jirutka.rsql.parser.ast.Node;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import solontax.g1.management.common.dto.PersonQueryParams;
import solontax.g1.management.domain.model.Person;
import solontax.g1.management.domain.port.PersonRepositoryPort;
import solontax.g1.management.infrastructure.adapter.out.entity.PersonEntity;
import solontax.g1.management.infrastructure.filter.RSQLCustomVisitor;
import solontax.g1.management.infrastructure.repository.PersonJpaRepository;

@Component
@AllArgsConstructor
@Log4j2
public class PersonJpaAdapter implements PersonRepositoryPort {
    private final PersonJpaRepository repository;

    private PersonEntity toPersonEntity(Person person) {
        return PersonEntity
                .builder()
                .firstName(person.getFirstName())
                .lastName(person.getLastName())
                .dateOfBirth(person.getDateOfBirth())
                .taxNumber(person.getTaxNumber())
                .taxDebt(person.getTaxDebt())
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
                .taxDebt(entity.getTaxDebt())
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

    @Override
    public Page<Person> search(PersonQueryParams personQueryParams) {
        Specification<PersonEntity> specification = null;

        if (personQueryParams.getQuery() != null) {
            String query = personQueryParams.getQuery();
            Node rootNode = new RSQLParser().parse(query);
            specification = rootNode.accept(new RSQLCustomVisitor<>());
        }

        Sort sort = Objects.equals(personQueryParams
                .getSortDirection(), "desc") ?
                Sort.by(personQueryParams.getSortBy()).descending() :
                Sort.by(personQueryParams.getSortBy()).ascending();

        Pageable pageable = PageRequest.of(
                personQueryParams.getOffset(),
                personQueryParams.getSize(),
                sort
        );

        Page<PersonEntity> persons = repository.findAll(specification, pageable);
        return persons.map(this::toPerson);
    }
}
