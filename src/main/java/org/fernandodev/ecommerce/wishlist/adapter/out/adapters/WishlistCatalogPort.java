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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adaptador de salida que implementa el puerto {@link CatalogPort}
 * utilizado por el contexto de Wishlist para consultar información
 * del catálogo de productos.
 *
 * <p>Este adaptador conecta el contexto de Wishlist con el contexto
 * {@code Product Catalog} mediante {@link ProductUseCases}, evitando
 * que el dominio de Wishlist dependa directamente de las implementaciones
 * internas del catálogo.</p>
 *
 * <p>El adaptador transforma el {@code Product} obtenido del catálogo
 * en un {@link ProductInfo}, exponiendo únicamente la información que
 * el contexto de Wishlist necesita para validar la existencia y
 * disponibilidad del producto y conocer sus variantes.</p>
 *
 * <p>Si el producto solicitado no existe, la excepción
 * {@link ResourceNotFoundException} se transforma en
 * {@link Optional#empty()}, evitando propagar detalles del contexto
 * de catálogo hacia el consumidor del puerto.</p>
 */
@Component
@RequiredArgsConstructor
public class WishlistCatalogPort implements CatalogPort {

    /**
     * Puerto de entrada utilizado para consultar productos del contexto
     * {@code Product Catalog}.
     */
    private final ProductUseCases products;

    /**
     * Busca información de un producto en el catálogo.
     *
     * <p>Además de verificar la existencia del producto, obtiene los
     * identificadores de sus variantes y determina si el producto
     * puede considerarse activo en el catálogo.</p>
     *
     * <p>Un producto se considera activo para efectos de Wishlist cuando
     * se encuentra activo y no ha sido marcado como eliminado.</p>
     *
     * <p>Cuando el producto no existe, el resultado es
     * {@link Optional#empty()} en lugar de propagar
     * {@link ResourceNotFoundException}.</p>
     *
     * @param productId identificador único del producto que se desea consultar.
     * @return información del producto encapsulada en {@link Optional},
     *         o un {@code Optional} vacío si el producto no existe.
     */
    @Override
    public Optional<ProductInfo> findProduct(ProductId productId) {
        try {
            Product product = products.getById(productId);

            Set<ProductVariantId> variantIds = product.getVariants()
                    .stream()
                    .map(org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariant::id)
                    .collect(Collectors.toSet());

            return Optional.of(
                    new ProductInfo(
                            product.getId(),
                            product.isActive() && !product.isDeleted(),
                            variantIds
                    )
            );
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }
}
