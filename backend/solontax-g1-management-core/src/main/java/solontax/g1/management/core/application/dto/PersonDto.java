package solontax.g1.management.core.application.dto;

import lombok.*;

import java.util.UUID;

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

    private Long taxDebt;
}
