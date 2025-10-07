package solontax.g1.hexmanagement.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CommonException extends RuntimeException {
    private final HttpStatus status;

    private final String message;

    @Builder
    public CommonException(String message, HttpStatus status) {
        super(message);
        this.status = status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = message;
    }
}
