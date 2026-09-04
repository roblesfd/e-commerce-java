package org.fernandodev.ecommerce.admin.application.port.out;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.admin.domain.SystemConfig;

/**
 * Puerto de salida de persistencia de la configuración del sistema.
 */
public interface SystemConfigRepository {

    /**
     * Persiste la configuración.
     *
     * @param config configuración a guardar.
     * @return la configuración persistida.
     */
    SystemConfig save(SystemConfig config);

    /**
     * @param key clave de configuración.
     * @return la configuración, o {@code Optional.empty()} si no existe.
     */
    Optional<SystemConfig> findById(String key);

    /**
     * @return todas las configuraciones del sistema.
     */
    List<SystemConfig> findAll();
}