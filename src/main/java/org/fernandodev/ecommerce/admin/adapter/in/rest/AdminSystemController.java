package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminSystemUseCases;
import org.fernandodev.ecommerce.admin.domain.AuditEntry;
import org.fernandodev.ecommerce.admin.domain.SystemConfig;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de configuración, auditoría y operaciones administrativas.
 * <p>
 * Permite gestionar la configuración del sistema, consultar la auditoría y
 * las métricas de operación. Todos los endpoints están protegidos y requieren
 * rol y grupo de administrador.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminSystemController {

    private final AdminSystemUseCases adminSystem;

    /**
     * Establece el valor de una clave de configuración.
     *
     * @param key clave de configuración.
     * @param body valor a asignar.
     * @return la configuración guardada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/config/{key}")
    public SystemConfig setConfig(@PathVariable String key, @RequestBody ConfigValue body) {
        return adminSystem.setConfig(key, body.value());
    }

    /**
     * Consulta el valor de una clave de configuración.
     *
     * @param key clave de configuración.
     * @return la configuración encontrada.
     * @throws ResourceNotFoundException si la clave no está configurada.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/config/{key}")
    public SystemConfig getConfig(@PathVariable String key) {
        return adminSystem.getConfig(key)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada: " + key));
    }

    /**
     * Lista todas las claves de configuración.
     *
     * @return las configuraciones existentes.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/config")
    public List<SystemConfig> listConfig() {
        return adminSystem.listConfig();
    }

    /**
     * Consulta los registros de auditoría, opcionalmente filtrados.
     *
     * @param action  acción a filtrar (opcional).
     * @param actorId identificador del actor a filtrar (opcional).
     * @return los registros de auditoría.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/audit")
    public List<AuditEntry> listAudit(@RequestParam(required = false) String action,
                                      @RequestParam(required = false) UUID actorId) {
        return adminSystem.listAudit(action, actorId);
    }

    /**
     * Consulta las métricas de operación del sistema.
     *
     * @return las métricas de operación.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/operations/metrics")
    public AdminSystemUseCases.Metrics metrics() {
        return adminSystem.metrics();
    }

    /**
     * Consulta el número de fallos de integración registrados.
     *
     * @return el conteo de fallos de integración.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/operations/integration-failures")
    public IntegrationFailures integrationFailures() {
        return new IntegrationFailures(adminSystem.integrationFailures());
    }

    /**
     * Valor a asignar a una clave de configuración.
     *
     * @param value valor de la configuración.
     */
    public record ConfigValue(String value) {
    }

    /**
     * Conteo de fallos de integración.
     *
     * @param count número de fallos.
     */
    public record IntegrationFailures(int count) {
    }
}