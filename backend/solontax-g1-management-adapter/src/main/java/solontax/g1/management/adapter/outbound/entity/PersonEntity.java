package solontax.g1.management.adapter.outbound.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

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
