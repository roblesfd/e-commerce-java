package org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa;

import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.adapter.out.persistence.jpa.entity.InventoryJpaEntity;
import org.fernandodev.ecommerce.inventory.adapter.util.mapper.InventoryPersistenceMapper;
import org.fernandodev.ecommerce.inventory.application.port.out.InventoryRepository;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.InventoryId;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link InventoryRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaInventoryRepository implements InventoryRepository {

    private final InventorySpringDataRepository springData;

    @Override
    public Inventory save(Inventory inventory) {
        InventoryJpaEntity saved = springData.save(InventoryPersistenceMapper.toEntity(inventory));
        return InventoryPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Inventory> findById(InventoryId id) {
        return springData.findById(id.value()).map(InventoryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Inventory> findByProductAndVariant(ProductId productId, ProductVariantId variantId) {
        return springData.findByProductIdAndVariantId(
                        productId.value(),
                        variantId == null ? null : variantId.value())
                .map(InventoryPersistenceMapper::toDomain);
    }

    @Override
    public Optional<Inventory> findByProductAndVariantForUpdate(ProductId productId, ProductVariantId variantId) {
        return springData.findByProductAndVariantForUpdate(
                        productId.value(),
                        variantId == null ? null : variantId.value())
                .map(InventoryPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsByProductAndVariant(ProductId productId, ProductVariantId variantId) {
        return springData.existsByProductIdAndVariantId(
                productId.value(),
                variantId == null ? null : variantId.value());
    }
}