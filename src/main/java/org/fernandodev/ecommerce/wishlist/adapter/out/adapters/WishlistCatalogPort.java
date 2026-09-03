package org.fernandodev.ecommerce.wishlist.adapter.out.adapters;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.fernandodev.ecommerce.wishlist.application.port.out.CatalogPort;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador del puerto {@link CatalogPort} hacia el contexto Product Catalog.
 */
@Component
@RequiredArgsConstructor
public class WishlistCatalogPort implements CatalogPort {

    private final ProductUseCases products;

    @Override
    public Optional<ProductInfo> findProduct(ProductId productId) {
        try {
            Product product = products.getById(productId);
            Set<ProductVariantId> variantIds = product.getVariants().stream()
                    .map(org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariant::id)
                    .collect(Collectors.toSet());
            return Optional.of(new ProductInfo(
                    product.getId(),
                    product.isActive() && !product.isDeleted(),
                    variantIds));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }
}