package org.fernandodev.ecommerce.wishlist.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.wishlist.adapter.out.persistence.jpa.entity.WishlistJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio Spring Data JPA del agregado {@link WishlistJpaEntity}.
 */
public interface WishlistSpringDataRepository extends JpaRepository<WishlistJpaEntity, UUID> {

    List<WishlistJpaEntity> findByCustomerIdOrderByCreatedAtAsc(UUID customerId);
}