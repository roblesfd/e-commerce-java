package org.fernandodev.ecommerce.wishlist.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.wishlist.adapter.out.persistence.jpa.entity.WishlistJpaEntity;
import org.fernandodev.ecommerce.wishlist.adapter.util.mapper.WishlistPersistenceMapper;
import org.fernandodev.ecommerce.wishlist.application.port.out.WishlistRepository;
import org.fernandodev.ecommerce.wishlist.domain.Wishlist;
import org.fernandodev.ecommerce.wishlist.domain.value_object.WishlistId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link WishlistRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaWishlistRepository implements WishlistRepository {

    private final WishlistSpringDataRepository springData;

    @Override
    public Wishlist save(Wishlist wishlist) {
        WishlistJpaEntity saved = springData.save(WishlistPersistenceMapper.toEntity(wishlist));
        return WishlistPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Wishlist> findById(WishlistId id) {
        return springData.findById(id.value()).map(WishlistPersistenceMapper::toDomain);
    }

    @Override
    public List<Wishlist> findByCustomerId(UUID customerId) {
        return springData.findByCustomerIdOrderByCreatedAtAsc(customerId).stream()
                .map(WishlistPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void delete(WishlistId id) {
        springData.deleteById(id.value());
    }
}