package org.fernandodev.ecommerce.checkout.application.port.out;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.checkout.application.dto.ProductInfo;

/**
 * Proveedor del producto (contexto catalog): valida existencia y aporta
 * snapshots de SKU/nombre.
 */
public interface ProductProvider {

/**
     * Valida la existencia del producto y aporta su snapshot.
     *
     * @param productId producto.
     * @return SKU y nombre del producto.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException
     *         si el producto no existe.
     */
    ProductInfo productInfoOf(ProductId productId);
}