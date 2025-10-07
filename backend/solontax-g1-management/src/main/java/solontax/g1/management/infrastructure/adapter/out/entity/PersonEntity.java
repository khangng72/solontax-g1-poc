package solontax.g1.management.infrastructure.adapter.out.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "persons")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PersonEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    @Column(updatable = false, unique = true)
    private Long taxNumber;

    @Column(nullable = false)
    private Long taxDebt;

    @PrePersist
    public void setDefaultValueForTaxDebt() {
        if (taxDebt == null) {
            taxDebt = 0L;
        }
    }
}
