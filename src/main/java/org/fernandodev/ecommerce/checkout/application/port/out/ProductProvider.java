package org.fernandodev.ecommerce.checkout.application.port.out;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.checkout.application.dto.ProductInfo;

/**
 * Proveedor del producto (contexto catalog): valida existencia y aporta
 * snapshots de SKU/nombre.
 */
public interface ProductProvider {

    ProductInfo productInfoOf(ProductId productId);
}