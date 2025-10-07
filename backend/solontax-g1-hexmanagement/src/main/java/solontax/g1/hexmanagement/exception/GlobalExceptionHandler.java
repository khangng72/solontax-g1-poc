package solontax.g1.hexmanagement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice // Use this instead of @ControllerAdvice for JSON APIs
public class GlobalExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<CommonExceptionDto> handleCommonException(CommonException exception) {
        CommonExceptionDto exceptionDto = CommonExceptionDto.builder()
                                                            .status(exception.getStatus())
                                                            .message(exception.getMessage())
                                                            .build();

        return ResponseEntity.status(exception.getStatus()).body(exceptionDto);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonExceptionDto> handleIllegalArgument(IllegalArgumentException exception) {
        CommonExceptionDto exceptionDto = CommonExceptionDto.builder()
                                                            .status(HttpStatus.BAD_REQUEST)
                                                            .message(exception.getMessage())
                                                            .build();

        return ResponseEntity.badRequest().body(exceptionDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonExceptionDto> handleGeneralException(Exception exception) {
        CommonExceptionDto exceptionDto = CommonExceptionDto.builder()
                                                            .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                            .message("An unexpected error occurred: " + exception.getMessage())
                                                            .build();

        return ResponseEntity.internalServerError().body(exceptionDto);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<CommonExceptionDto> handleNoHandlerFound(NoHandlerFoundException ex) {
        CommonExceptionDto dto = CommonExceptionDto.builder()
                                                   .status(HttpStatus.NOT_FOUND)
                                                   .message("No endpoint found: " + ex.getRequestURL())
                                                   .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }

}
