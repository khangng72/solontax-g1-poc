package solontax.g1.management.core.domain.port;

import solontax.g1.management.core.domain.model.AuditLog;

public interface AuditLogRepositoryPort {
    void save(AuditLog auditLog);
}
