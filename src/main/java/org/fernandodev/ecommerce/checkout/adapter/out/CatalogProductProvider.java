package org.fernandodev.ecommerce.checkout.adapter.out;

import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.checkout.application.dto.ProductInfo;
import org.fernandodev.ecommerce.checkout.application.port.out.ProductProvider;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link ProductProvider} consultando el contexto
 * catalog.
 */
@Component
@RequiredArgsConstructor
public class CatalogProductProvider implements ProductProvider {

    private final ProductUseCases productUseCases;

    @Override
    public ProductInfo productInfoOf(ProductId productId) {
        Product product = productUseCases.getById(productId);
        return new ProductInfo(product.getSku().value(), product.getName());
    }
}