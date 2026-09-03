package org.fernandodev.ecommerce.admin.application.port.in;

import org.fernandodev.ecommerce.catalog.application.dto.BrandCommand;
import org.fernandodev.ecommerce.catalog.application.dto.CategoryCommand;
import org.fernandodev.ecommerce.catalog.application.dto.ProductCommand;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;

/**
 * Orquestación administrativa del catálogo.
 */
public interface AdminCatalogUseCases {

    Product createProduct(ProductCommand command);

    Product updateProduct(ProductId id, ProductCommand command);

    Product activateProduct(ProductId id);

    Product deactivateProduct(ProductId id);

    void deleteProduct(ProductId id);

    Category createCategory(CategoryCommand command);

    Category updateCategory(CategoryId id, CategoryCommand command);

    Category activateCategory(CategoryId id);

    Category deactivateCategory(CategoryId id);

    void deleteCategory(CategoryId id);

    Brand createBrand(BrandCommand command);

    Brand updateBrand(BrandId id, BrandCommand command);

    Brand activateBrand(BrandId id);

    Brand deactivateBrand(BrandId id);

    void deleteBrand(BrandId id);
}