package solontax.g1.hexmanagement.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import solontax.g1.hexmanagement.infrastructure.adapter.out.entity.PersonEntity;

public interface PersonJpaRepository extends JpaRepository<PersonEntity, UUID> {
    Optional<PersonEntity> findByTaxNumber(Long taxNumber);
}
