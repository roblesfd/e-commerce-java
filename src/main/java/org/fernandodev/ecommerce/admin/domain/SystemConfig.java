package org.fernandodev.ecommerce.admin.domain;

import java.time.Instant;
import java.util.Objects;

import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Configuración del sistema gestionada por administradores (clave → valor).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemConfig {

    private String key;
    private String value;
    private Instant updatedAt;

    public static SystemConfig create(String key, String value, Instant at) {
        SystemConfig config = new SystemConfig();
        config.key = validateKey(key);
        config.value = Objects.requireNonNull(value, "value no puede ser null");
        config.updatedAt = Objects.requireNonNull(at, "at no puede ser null");
        return config;
    }

    public static SystemConfig crear(String key, String value, Instant updatedAt) {
        return create(key, value, updatedAt);
    }

    public void update(String value, Instant at) {
        this.value = Objects.requireNonNull(value, "value no puede ser null");
        this.updatedAt = at;
    }

    private static String validateKey(String key) {
        Objects.requireNonNull(key, "key no puede ser null");
        if (key.isBlank()) {
            throw new BusinessRuleViolationException("La clave de configuración no puede estar vacía");
        }
        return key.trim();
    }
}