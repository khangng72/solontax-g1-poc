package solontax.g1.management.core.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class TaxCalculationDto {
    @NotNull
    private Long taxNumber;
    @NotNull
    private Long calculatedTax;
}
