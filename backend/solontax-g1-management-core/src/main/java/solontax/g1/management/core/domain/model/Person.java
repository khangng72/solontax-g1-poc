package solontax.g1.management.core.domain.model;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Person {
    private UUID id;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private Long taxNumber;

    private Long taxDebt;
}
