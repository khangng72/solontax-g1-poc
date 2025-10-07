package solontax.g1.hexmanagement.domain.model;

import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Person {
    private UUID id;

    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private Long taxNumber;
}
