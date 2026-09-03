package org.fernandodev.ecommerce.search.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Puerto hacia el contexto Product Catalog para construir el índice.
 */
public interface CatalogPort {

    Optional<ProductInfo> findProduct(ProductId productId);

    List<ProductId> listAllProductIds();

    record ProductInfo(
            ProductId id,
            String name,
            String description,
            String sku,
            String barcode,
            Set<CategoryId> categoryIds,
            UUID brandId,
            boolean active,
            boolean deleted,
            List<VariantInfo> variants,
            List<AttributeInfo> attributes
    ) {
    }

    record VariantInfo(ProductVariantId id, String sku, String barcode) {
    }

    record AttributeInfo(String name, String value) {
    }
}