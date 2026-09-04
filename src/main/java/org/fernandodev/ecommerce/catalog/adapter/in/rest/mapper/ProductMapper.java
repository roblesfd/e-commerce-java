package org.fernandodev.ecommerce.catalog.adapter.in.rest.mapper;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.PageResponse;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.ProductRequest;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.ProductResponse;
import org.fernandodev.ecommerce.catalog.adapter.in.rest.dto.ProductSummaryResponse;
import org.fernandodev.ecommerce.catalog.application.dto.PageResult;
import org.fernandodev.ecommerce.catalog.application.dto.ProductCommand;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductAttribute;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariant;
import org.springframework.stereotype.Component;

/**
 * Mapper manual entre DTOs REST y objetos de aplicación/dominio del contexto
 * Product Catalog. Sin lógica de negocio.
 */
@Component
public class ProductMapper {

    /**
     * @param request petición de producto.
     * @return comando de aplicación equivalente.
     */
    public ProductCommand toCommand(ProductRequest request) {
        return new ProductCommand(
                request.sku(),
                request.barcode(),
                request.name(),
                request.description(),
                request.brandId(),
                request.categoryIds(),
                request.variants().stream()
                        .map(variant -> new ProductCommand.VariantCommand(
                                variant.sku(),
                                variant.barcode(),
                                variant.attributes().stream()
                                        .map(attribute -> new ProductCommand.AttributeCommand(
                                                attribute.name(), attribute.value()))
                                        .toList()))
                        .toList(),
                request.attributes().stream()
                        .map(attribute -> new ProductCommand.AttributeCommand(
                                attribute.name(), attribute.value()))
                        .toList(),
                request.images());
    }

    /**
     * @param product agregado de dominio.
     * @return respuesta REST con el detalle completo del producto.
     */
    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId().value(),
                product.getSku().value(),
                product.getBarcode() == null ? null : product.getBarcode().value(),
                product.getName(),
                product.getDescription(),
                product.isActive(),
                product.getBrandId() == null ? null : product.getBrandId().value(),
                product.getCategoryIds().stream().map(CategoryId::value).collect(Collectors.toSet()),
                product.getVariants().stream().map(this::toVariantResponse).toList(),
                product.getAttributes().stream().map(this::toAttributeResponse).toList(),
                product.getImages().stream().map(image -> image.url()).toList());
    }

    /**
     * @param product agregado de dominio.
     * @return resumen del producto para listados.
     */
    public ProductSummaryResponse toSummaryResponse(Product product) {
        return new ProductSummaryResponse(
                product.getId().value(),
                product.getSku().value(),
                product.getName(),
                product.isActive());
    }

    /**
     * @param page página de productos de dominio.
     * @return respuesta REST paginada de resúmenes.
     */
    public PageResponse<ProductSummaryResponse> toPageResponse(PageResult<Product> page) {
        return new PageResponse<>(
                page.content().stream().map(this::toSummaryResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages());
    }

    /**
     * @param variant variante de dominio.
     * @return respuesta REST con la variante.
     */
    private ProductResponse.VariantResponse toVariantResponse(ProductVariant variant) {
        return new ProductResponse.VariantResponse(
                variant.id().value(),
                variant.sku() == null ? null : variant.sku().value(),
                variant.barcode() == null ? null : variant.barcode().value(),
                variant.attributes().stream().map(this::toAttributeResponse).toList());
    }

    /**
     * @param attribute atributo de dominio.
     * @return respuesta REST con el atributo.
     */
    private ProductResponse.AttributeResponse toAttributeResponse(ProductAttribute attribute) {
        return new ProductResponse.AttributeResponse(attribute.name(), attribute.value());
    }
}