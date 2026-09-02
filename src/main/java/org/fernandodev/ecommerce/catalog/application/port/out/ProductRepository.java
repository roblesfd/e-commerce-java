package org.fernandodev.ecommerce.catalog.application.port.out;

import java.util.Optional;

import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.dto.PageResult;
import org.fernandodev.ecommerce.catalog.application.dto.ProductSearch;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.Barcode;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;

/**
 * Puerto de salida de persistencia del agregado {@link Product}.
 * <p>
 * Las consultas de existencia excluyen los productos eliminados (soft delete).
 * </p>
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(ProductId id);

    boolean existsBySku(Sku sku);

    boolean existsByBarcode(Barcode barcode);

    boolean existsByBrandId(BrandId brandId);

    boolean existsByCategoryId(CategoryId categoryId);

    PageResult<Product> search(ProductSearch search, PageQuery query);
}