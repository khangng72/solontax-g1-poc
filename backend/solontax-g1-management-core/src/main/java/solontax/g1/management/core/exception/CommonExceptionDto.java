package solontax.g1.management.core.exception;


import lombok.*;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CommonExceptionDto {
    private HttpStatus status;
    private String message;
}
