package org.fernandodev.ecommerce.inventory.application.port.out;

import java.util.Optional;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.InventoryId;

/**
 * Puerto de salida de persistencia del agregado {@link Inventory}.
 */
public interface InventoryRepository {

    Inventory save(Inventory inventory);

    Optional<Inventory> findById(InventoryId id);

    Optional<Inventory> findByProductAndVariant(ProductId productId, ProductVariantId variantId);

    /**
     * Busca el inventario bloqueando la fila (PESSIMISTIC_WRITE) para operaciones
     * concurrentes sensibles (reservas y cambios de stock).
     */
    Optional<Inventory> findByProductAndVariantForUpdate(ProductId productId, ProductVariantId variantId);

    boolean existsByProductAndVariant(ProductId productId, ProductVariantId variantId);
}