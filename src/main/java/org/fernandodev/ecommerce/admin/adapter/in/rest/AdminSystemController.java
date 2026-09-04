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
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminSystemController {

    private final AdminSystemUseCases adminSystem;

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PutMapping("/config/{key}")
    public SystemConfig setConfig(@PathVariable String key, @RequestBody ConfigValue body) {
        return adminSystem.setConfig(key, body.value());
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/config/{key}")
    /**
     * Get config.
     * @param key key.
     */
    public SystemConfig getConfig(@PathVariable String key) {
        return adminSystem.getConfig(key)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada: " + key));
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/config")
    /**
     * List config.
     */
    public List<SystemConfig> listConfig() {
        return adminSystem.listConfig();
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/audit")
    /**
     * List audit.
     * @param action action.
     * @param actorId actorId.
     */
    public List<AuditEntry> listAudit(@RequestParam(required = false) String action,
                                      @RequestParam(required = false) UUID actorId) {
        return adminSystem.listAudit(action, actorId);
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/operations/metrics")
    /**
     * Metrics.
     */
    public AdminSystemUseCases.Metrics metrics() {
        return adminSystem.metrics();
    }

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/operations/integration-failures")
    /**
     * Integration failures.
     */
    public IntegrationFailures integrationFailures() {
        return new IntegrationFailures(adminSystem.integrationFailures());
    }

    /**

     * Config value.

     * @param value value.

     */

    public record ConfigValue(String value) {
    }

    /**

     * Integration failures.

     * @param count count.

     */

    public record IntegrationFailures(int count) {
    }
}