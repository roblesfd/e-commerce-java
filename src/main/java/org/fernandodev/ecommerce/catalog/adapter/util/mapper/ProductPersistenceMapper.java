package org.fernandodev.ecommerce.catalog.adapter.util.mapper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.ProductVariantJpaEntity;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.Barcode;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductAttribute;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductImage;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariant;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;

/**
 * Mapper manual entre el agregado de dominio {@link Product} y la entidad JPA
 * {@link ProductJpaEntity}. Sin lógica de negocio.
 */
public final class ProductPersistenceMapper {

    private ProductPersistenceMapper() {
    }

    /**
     * @param product agregado de dominio.
     * @return entidad JPA equivalente.
     */
    public static ProductJpaEntity toEntity(Product product) {
        ProductJpaEntity entity = new ProductJpaEntity();
        entity.setId(product.getId().value());
        entity.setSku(product.getSku().value());
        entity.setBarcode(product.getBarcode() == null ? null : product.getBarcode().value());
        entity.setName(product.getName());
        entity.setDescription(product.getDescription());
        entity.setActive(product.isActive());
        entity.setBrandId(product.getBrandId() == null ? null : product.getBrandId().value());
        entity.setDeletedAt(product.getDeletedAt().orElse(null));
        product.getCategoryIds().forEach(categoryId -> entity.getCategoryIds().add(categoryId.value()));
        product.getAttributes().forEach(attribute -> entity.getAttributes().put(attribute.name(), attribute.value()));
        product.getImages().forEach(image -> entity.getImages().add(image.url()));
        List<ProductVariant> variants = product.getVariants();
        for (int i = 0; i < variants.size(); i++) {
            entity.getVariants().add(toVariantEntity(variants.get(i), i, entity));
        }
        return entity;
    }

    /**
     * @param entity entidad JPA.
     * @return agregado de dominio equivalente.
     */
    public static Product toDomain(ProductJpaEntity entity) {
        return Product.crear(
                ProductId.of(entity.getId()),
                new Sku(entity.getSku()),
                entity.getBarcode() == null ? null : new Barcode(entity.getBarcode()),
                entity.getName(),
                entity.getDescription(),
                entity.isActive(),
                entity.getBrandId() == null ? null : BrandId.of(entity.getBrandId()),
                toCategoryIds(entity.getCategoryIds()),
                entity.getAttributes().entrySet().stream()
                        .map(entry -> new ProductAttribute(entry.getKey(), entry.getValue()))
                        .toList(),
                entity.getImages().stream().map(ProductImage::new).toList(),
                entity.getVariants().stream().map(ProductPersistenceMapper::toDomainVariant).toList(),
                entity.getDeletedAt());
    }

    private static ProductVariantJpaEntity toVariantEntity(ProductVariant variant, int position, ProductJpaEntity product) {
        ProductVariantJpaEntity entity = new ProductVariantJpaEntity();
        entity.setId(variant.id().value());
        entity.setProduct(product);
        entity.setPosition(position);
        entity.setSku(variant.sku() == null ? null : variant.sku().value());
        entity.setBarcode(variant.barcode() == null ? null : variant.barcode().value());
        variant.attributes().forEach(attribute -> entity.getAttributes().put(attribute.name(), attribute.value()));
        return entity;
    }

    private static ProductVariant toDomainVariant(ProductVariantJpaEntity entity) {
        return new ProductVariant(
                ProductVariantId.of(entity.getId()),
                entity.getSku() == null ? null : new Sku(entity.getSku()),
                entity.getBarcode() == null ? null : new Barcode(entity.getBarcode()),
                entity.getAttributes().entrySet().stream()
                        .map(entry -> new ProductAttribute(entry.getKey(), entry.getValue()))
                        .toList());
    }

    private static Set<CategoryId> toCategoryIds(Set<java.util.UUID> categoryIds) {
        Set<CategoryId> result = new LinkedHashSet<>();
        categoryIds.forEach(value -> result.add(CategoryId.of(value)));
        return result;
    }
}
