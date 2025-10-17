package solontax.g1.management.kafka.config.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import solontax.g1.management.core.exception.CommonException;

import java.util.Random;

@Slf4j
public class Utils {
    private static final Random random = new Random();

    private Utils() {
    }

    public static void generateRandomFailure(String message, double errorRate) {

        if (random.nextDouble() < errorRate) {
            log.error("Intended error: {}", message);
            throw new CommonException(
                    "Intended error: " + message,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
