package org.fernandodev.ecommerce.admin.application.service;

import org.fernandodev.ecommerce.admin.application.port.in.AdminCatalogUseCases;
import org.fernandodev.ecommerce.catalog.application.dto.BrandCommand;
import org.fernandodev.ecommerce.catalog.application.dto.CategoryCommand;
import org.fernandodev.ecommerce.catalog.application.dto.ProductCommand;
import org.fernandodev.ecommerce.catalog.application.port.in.BrandUseCases;
import org.fernandodev.ecommerce.catalog.application.port.in.CategoryUseCases;
import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Orquestación administrativa del catálogo. Delega en los casos de uso del
 * contexto Catalog (no salta sus reglas de dominio) y audita lo sensible.
 */
@RequiredArgsConstructor
public class AdminCatalogService implements AdminCatalogUseCases {

    private final ProductUseCases products;
    private final CategoryUseCases categories;
    private final BrandUseCases brands;
    private final AuditService audit;

    @Override
    @Transactional
    public Product createProduct(ProductCommand command) {
        return products.create(command);
    }

    @Override
    @Transactional
    public Product updateProduct(ProductId id, ProductCommand command) {
        return products.update(id, command);
    }

    @Override
    @Transactional
    public Product activateProduct(ProductId id) {
        return products.activate(id);
    }

    @Override
    @Transactional
    public Product deactivateProduct(ProductId id) {
        audit.record("DEACTIVATE_PRODUCT", "product", id.value(), "Producto desactivado");
        return products.deactivate(id);
    }

    @Override
    @Transactional
    public void deleteProduct(ProductId id) {
        audit.record("DELETE_PRODUCT", "product", id.value(), "Producto eliminado");
        products.delete(id);
    }

    @Override
    @Transactional
    public Category createCategory(CategoryCommand command) {
        return categories.create(command);
    }

    @Override
    @Transactional
    public Category updateCategory(CategoryId id, CategoryCommand command) {
        return categories.update(id, command);
    }

    @Override
    @Transactional
    public Category activateCategory(CategoryId id) {
        return categories.activate(id);
    }

    @Override
    @Transactional
    public Category deactivateCategory(CategoryId id) {
        audit.record("DEACTIVATE_CATEGORY", "category", id.value(), "Categoría desactivada");
        return categories.deactivate(id);
    }

    @Override
    @Transactional
    public void deleteCategory(CategoryId id) {
        audit.record("DELETE_CATEGORY", "category", id.value(), "Categoría eliminada");
        categories.delete(id);
    }

    @Override
    @Transactional
    public Brand createBrand(BrandCommand command) {
        return brands.create(command);
    }

    @Override
    @Transactional
    public Brand updateBrand(BrandId id, BrandCommand command) {
        return brands.update(id, command);
    }

    @Override
    @Transactional
    public Brand activateBrand(BrandId id) {
        return brands.activate(id);
    }

    @Override
    @Transactional
    public Brand deactivateBrand(BrandId id) {
        audit.record("DEACTIVATE_BRAND", "brand", id.value(), "Marca desactivada");
        return brands.deactivate(id);
    }

    @Override
    @Transactional
    public void deleteBrand(BrandId id) {
        audit.record("DELETE_BRAND", "brand", id.value(), "Marca eliminada");
        brands.delete(id);
    }
}