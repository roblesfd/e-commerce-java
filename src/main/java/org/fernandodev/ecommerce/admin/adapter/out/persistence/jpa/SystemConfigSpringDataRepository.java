package org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa;

import org.fernandodev.ecommerce.admin.adapter.out.persistence.jpa.entity.SystemConfigJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA de la configuración del sistema.
 */
public interface SystemConfigSpringDataRepository extends JpaRepository<SystemConfigJpaEntity, String> {
}