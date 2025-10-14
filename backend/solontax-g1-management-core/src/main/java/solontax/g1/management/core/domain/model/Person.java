package solontax.g1.management.core.domain.model;

import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private Long taxNumber;

    private Long taxDebt;
}
