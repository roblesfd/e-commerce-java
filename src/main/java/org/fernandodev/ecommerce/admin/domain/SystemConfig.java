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

    /**
     * Crea una configuración de sistema.
     *
     * @param key   clave de configuración.
     * @param value valor.
     * @param at    momento de creación.
     * @return la configuración creada.
     * @throws BusinessRuleViolationException si la clave está vacía.
     * @throws NullPointerException           si {@code value} o {@code at} son {@code null}.
     */
    public static SystemConfig create(String key, String value, Instant at) {
        SystemConfig config = new SystemConfig();
        config.key = validateKey(key);
        config.value = Objects.requireNonNull(value, "value no puede ser null");
        config.updatedAt = Objects.requireNonNull(at, "at no puede ser null");
        return config;
    }

    /**
     * Alias de {@link #create(String, String, Instant)} usado por la persistencia.
     *
     * @return la configuración creada.
     */
    public static SystemConfig crear(String key, String value, Instant updatedAt) {
        return create(key, value, updatedAt);
    }

    /**
     * Actualiza el valor de la configuración.
     *
     * @param value nuevo valor.
     * @param at    momento de la actualización.
     * @throws NullPointerException si {@code value} es {@code null}.
     */
    public void update(String value, Instant at) {
        this.value = Objects.requireNonNull(value, "value no puede ser null");
        this.updatedAt = at;
    }

    /**
     * Valida que la clave no esté vacía.
     *
     * @param key clave a validar.
     * @return la clave normalizada.
     * @throws BusinessRuleViolationException si la clave está vacía.
     */
    private static String validateKey(String key) {
        Objects.requireNonNull(key, "key no puede ser null");
        if (key.isBlank()) {
            throw new BusinessRuleViolationException("La clave de configuración no puede estar vacía");
        }
        return key.trim();
    }
}