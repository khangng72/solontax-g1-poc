package solontax.g1.management.core.domain.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class AuditLog {
    private UUID id;

    private String methodName;

    private String arguments;

    private String result;

    private long durationInMs;

    private String exception;

    private LocalDateTime createdAt;
}
