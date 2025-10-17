package solontax.g1.management.dao.outbound.jpa;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import solontax.g1.management.core.domain.model.AuditLog;
import solontax.g1.management.core.domain.port.AuditLogRepositoryPort;
import solontax.g1.management.dao.outbound.entity.AuditLogEntity;
import solontax.g1.management.dao.repository.AuditLogJpaRepository;

@Component
@RequiredArgsConstructor
public class AuditLogLogJpaAdapter implements AuditLogRepositoryPort {
    private final AuditLogJpaRepository auditLogJpaRepository;

    private AuditLogEntity toAuditLogEntity(AuditLog auditLog) {
        return AuditLogEntity.builder()
                .id(auditLog.getId())
                .methodName(auditLog.getMethodName())
                .arguments(auditLog.getArguments())
                .result(auditLog.getResult())
                .durationInMs(auditLog.getDurationInMs())
                .exception(auditLog.getException())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }

    @Override
    public void save(AuditLog auditLog) {
        AuditLogEntity auditLogEntity = toAuditLogEntity(auditLog);
        auditLogJpaRepository.save(auditLogEntity);
    }
}
