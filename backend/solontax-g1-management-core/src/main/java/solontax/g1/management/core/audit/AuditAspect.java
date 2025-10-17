package solontax.g1.management.core.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import solontax.g1.management.core.domain.model.AuditLog;
import solontax.g1.management.core.domain.port.AuditLogRepositoryPort;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {
    private final AuditLogRepositoryPort auditLogRepository;

     @Around(
        "@annotation(solontax.g1.management.core.audit.Audit) || " +
        "@within(solontax.g1.management.core.audit.Audit) || " +
        "within(@org.springframework.web.bind.annotation.RestController *) || " +
        "@annotation(org.springframework.kafka.annotation.KafkaListener)"
    )
    public Object auditAll(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            saveAuditLog(methodName, args, result, null, duration);
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            saveAuditLog(methodName, args, null, e, duration);
            throw e;
        }
    }

    private void saveAuditLog(
            String methodName,
            Object[] args,
            Object result,
            Exception exception,
            long duration) {
        try {
            AuditLog logEntry = AuditLog.builder()
                    .methodName(methodName)
                    .arguments(Arrays.toString(args))
                    .result(result != null ? result.toString() : null)
                    .exception(exception != null ? exception.getMessage() : null)
                    .durationInMs(duration)
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
}
