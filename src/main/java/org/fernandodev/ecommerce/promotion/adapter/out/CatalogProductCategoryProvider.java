package org.fernandodev.ecommerce.promotion.adapter.out;

import java.util.Set;

import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.promotion.application.port.out.ProductCategoryProvider;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Adaptador que implementa {@link ProductCategoryProvider} consultando el
 * contexto catalog (puerto de entrada de su capa de aplicación).
 */
@Component
@RequiredArgsConstructor
public class CatalogProductCategoryProvider implements ProductCategoryProvider {

    private final ProductUseCases productUseCases;

    @Override
    public Set<CategoryId> categoriesOf(ProductId productId) {
        return productUseCases.getById(productId).getCategoryIds();
    }
}