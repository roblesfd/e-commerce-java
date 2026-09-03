package org.fernandodev.ecommerce.admin.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.admin.domain.SystemConfig;

/**
 * Puerto de salida de persistencia de la configuración del sistema.
 */
public interface SystemConfigRepository {

    SystemConfig save(SystemConfig config);

    Optional<SystemConfig> findById(String key);

    List<SystemConfig> findAll();
}