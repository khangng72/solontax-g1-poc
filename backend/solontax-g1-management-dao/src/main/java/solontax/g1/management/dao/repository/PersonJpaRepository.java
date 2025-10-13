package solontax.g1.management.dao.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import solontax.g1.management.dao.outbound.entity.PersonEntity;

import java.util.Optional;
import java.util.UUID;

public interface PersonJpaRepository extends JpaRepository<PersonEntity, UUID> {
    Optional<PersonEntity> findByTaxNumber(Long taxNumber);

    Page<PersonEntity> findAll(Specification<PersonEntity> spec, Pageable pageable);
}
