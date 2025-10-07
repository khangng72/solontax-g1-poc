package solontax.g1.hexmanagement.application.dto;

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
public class PersonDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private Long age;
    private Long taxNumber;
}
