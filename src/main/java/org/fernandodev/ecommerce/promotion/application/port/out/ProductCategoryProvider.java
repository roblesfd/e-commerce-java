package org.fernandodev.ecommerce.promotion.application.port.out;

import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;

/**
 * Proveedor de las categorías de un producto (contexto catalog).
 */
public interface ProductCategoryProvider {

    Set<CategoryId> categoriesOf(ProductId productId);
}