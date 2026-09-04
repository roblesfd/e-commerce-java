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

    /**
     * Persiste el inventario.
     *
     * @param inventory inventario a guardar.
     * @return el inventario persistido.
     */
    Inventory save(Inventory inventory);

    /**
     * @param id identificador del inventario.
     * @return el inventario, o {@code Optional.empty()} si no existe.
     */
    Optional<Inventory> findById(InventoryId id);

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return el inventario del producto/variante, o {@code Optional.empty()} si no existe.
     */
    Optional<Inventory> findByProductAndVariant(ProductId productId, ProductVariantId variantId);

    /**
     * Busca el inventario bloqueando la fila (PESSIMISTIC_WRITE) para operaciones
     * concurrentes sensibles (reservas y cambios de stock).
     *
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return el inventario bloqueado, o {@code Optional.empty()} si no existe.
     */
    Optional<Inventory> findByProductAndVariantForUpdate(ProductId productId, ProductVariantId variantId);

    /**
     * @param productId producto.
     * @param variantId variante (null si el stock es a nivel producto).
     * @return {@code true} si existe un inventario para el producto/variante.
     */
    boolean existsByProductAndVariant(ProductId productId, ProductVariantId variantId);
}