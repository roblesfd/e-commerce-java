package org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA de la configuración del sistema.
 */
@Entity
@Table(name = "system_config")
@Getter
@Setter
@NoArgsConstructor
public class SystemConfigJpaEntity {

    @Id
    @Column(name = "config_key", length = 100)
    private String key;

    @Column(name = "value", nullable = false, length = 500)
    private String value;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;
}