package org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.shipping.adapter.out.persistence.jpa.entity.ShippingMethodJpaEntity;
import org.fernandodev.ecommerce.shipping.adapter.util.mapper.ShippingMethodPersistenceMapper;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingMethodRepository;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link ShippingMethodRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaShippingMethodRepository implements ShippingMethodRepository {

    private final ShippingMethodSpringDataRepository springData;

    @Override
    public ShippingMethod save(ShippingMethod method) {
        ShippingMethodJpaEntity saved = springData.save(ShippingMethodPersistenceMapper.toEntity(method));
        return ShippingMethodPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<ShippingMethod> findById(ShippingMethodId id) {
        return springData.findById(id.value()).map(ShippingMethodPersistenceMapper::toDomain);
    }

    @Override
    public List<ShippingMethod> findAll() {
        return springData.findAll().stream().map(ShippingMethodPersistenceMapper::toDomain).toList();
    }

    @Override
    public boolean existsById(ShippingMethodId id) {
        return springData.existsById(id.value());
    }
}