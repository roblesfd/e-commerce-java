package org.fernandodev.ecommerce.admin.application.port.in;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.domain.AuditEntry;
import org.fernandodev.ecommerce.admin.domain.SystemConfig;

/**
 * Configuración del sistema, auditoría y operaciones administrativas.
 */
public interface AdminSystemUseCases {

    SystemConfig setConfig(String key, String value);

    Optional<SystemConfig> getConfig(String key);

    List<SystemConfig> listConfig();

    List<AuditEntry> listAudit(String action, UUID actorId);

    Metrics metrics();

    int integrationFailures();

    record Metrics(
            long customers,
            long products,
            long categories,
            long brands,
            long orders,
            long payments,
            long returns,
            long pendingReviews
    ) {
    }
}