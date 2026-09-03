package org.fernandodev.ecommerce.search.adapter.out.adapters;

import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.dto.ProductSearch;
import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.search.application.port.out.CatalogPort;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador del puerto {@link CatalogPort} hacia el contexto Product Catalog.
 */
@Component
@RequiredArgsConstructor
public class SearchCatalogPort implements CatalogPort {

    private final ProductUseCases products;

    @Override
    public Optional<ProductInfo> findProduct(ProductId productId) {
        try {
            Product product = products.getById(productId);
            List<VariantInfo> variants = product.getVariants().stream()
                    .map(variant -> new VariantInfo(
                            variant.id(),
                            variant.sku() == null ? null : variant.sku().value(),
                            variant.barcode() == null ? null : variant.barcode().value()))
                    .toList();
            List<AttributeInfo> attributes = product.getAttributes().stream()
                    .map(attribute -> new AttributeInfo(attribute.name(), attribute.value()))
                    .toList();
            return Optional.of(new ProductInfo(
                    product.getId(),
                    product.getName(),
                    product.getDescription(),
                    product.getSku() == null ? null : product.getSku().value(),
                    product.getBarcode() == null ? null : product.getBarcode().value(),
                    product.getCategoryIds(),
                    product.getBrandId() == null ? null : product.getBrandId().value(),
                    product.isActive(),
                    product.isDeleted(),
                    variants,
                    attributes));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<ProductId> listAllProductIds() {
        return products.list(ProductSearch.all(), new PageQuery(0, 10000, null, null)).content().stream()
                .map(Product::getId)
                .toList();
    }
}