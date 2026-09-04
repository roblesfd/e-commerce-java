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

    /**
     * Define o actualiza una configuración del sistema, registrando la acción en auditoría.
     *
     * @param key   clave de configuración.
     * @param value valor.
     * @return la configuración persistida.
     */
    SystemConfig setConfig(String key, String value);

    /**
     * @param key clave de configuración.
     * @return la configuración, o {@code Optional.empty()} si no existe.
     */
    Optional<SystemConfig> getConfig(String key);

    /**
     * @return todas las configuraciones del sistema.
     */
    List<SystemConfig> listConfig();

    /**
     * Consulta las entradas de auditoría, opcionalmente filtradas.
     *
     * @param action  acción a filtrar (opcional).
     * @param actorId actor a filtrar (opcional).
     * @return las entradas de auditoría.
     */
    List<AuditEntry> listAudit(String action, UUID actorId);

    /**
     * @return métricas operativas agregadas de los contextos.
     */
    Metrics metrics();

    /**
     * @return el número de fallos de indexación (integration failures).
     */
    int integrationFailures();

    /**
     * Conteos operativos por contexto para el panel de administración.
     *
     * @param customers      número de clientes.
     * @param products       número de productos.
     * @param categories     número de categorías.
     * @param brands         número de marcas.
     * @param orders         número de órdenes.
     * @param payments       número de pagos.
     * @param returns        número de devoluciones.
     * @param pendingReviews número de reviews pendientes de moderación.
     */
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