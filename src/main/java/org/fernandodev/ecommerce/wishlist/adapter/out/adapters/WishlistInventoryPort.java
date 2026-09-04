package org.fernandodev.ecommerce.wishlist.adapter.out.adapters;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.fernandodev.ecommerce.wishlist.application.port.out.InventoryPort;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa el puerto {@link InventoryPort}
 * utilizado por el contexto de Wishlist para consultar la disponibilidad
 * de inventario de los productos y sus variantes.
 *
 * <p>Este adaptador establece la comunicación entre el contexto de
 * Wishlist y el contexto {@code Inventory} mediante
 * {@link InventoryUseCases}, evitando que el dominio de Wishlist dependa
 * directamente de la implementación interna del inventario.</p>
 *
 * <p>Su responsabilidad se limita a delegar la consulta de stock al
 * caso de uso correspondiente y devolver al consumidor del puerto
 * la cantidad disponible.</p>
 */
@Component
@RequiredArgsConstructor
public class WishlistInventoryPort implements InventoryPort {

    /**
     * Puerto de entrada utilizado para consultar la disponibilidad
     * de inventario de productos y variantes.
     */
    private final InventoryUseCases inventory;

    /**
     * Obtiene la cantidad de unidades disponibles de un producto
     * o de una variante específica.
     *
     * <p>La consulta se delega directamente al contexto de
     * {@code Inventory}, manteniendo aislado al contexto de Wishlist
     * de los detalles de implementación del inventario.</p>
     *
     * @param productId identificador único del producto.
     * @param variantId identificador de la variante del producto,
     *                  cuando corresponda.
     * @return cantidad de unidades disponibles para el producto
     *         y variante indicados.
     */
    @Override
    public int availableStock(
            ProductId productId,
            ProductVariantId variantId
    ) {
        return inventory.availableStock(productId, variantId);
    }
}
