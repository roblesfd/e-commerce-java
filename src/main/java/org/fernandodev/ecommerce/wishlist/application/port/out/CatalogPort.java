package org.fernandodev.ecommerce.wishlist.application.port.out;

import java.util.Optional;
import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Puerto hacia el contexto Product Catalog: consultar productos y variantes
 * para validar los items de la wishlist.
 */
public interface CatalogPort {

    Optional<ProductInfo> findProduct(ProductId productId);

    record ProductInfo(ProductId id, boolean active, Set<ProductVariantId> variantIds) {
    }
}