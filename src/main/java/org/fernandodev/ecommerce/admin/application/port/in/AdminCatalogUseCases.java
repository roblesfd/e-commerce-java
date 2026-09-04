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

/**
     * Crea un producto delegando en el contexto Catalog.
     */
    Product createProduct(ProductCommand command);

/**
     * Actualiza un producto (semántica PUT).
     */
    Product updateProduct(ProductId id, ProductCommand command);

/**
     * Activa un producto.
     */
    Product activateProduct(ProductId id);

/**
     * Desactiva un producto y lo registra en auditoría.
     */
    Product deactivateProduct(ProductId id);

/**
     * Elimina un producto (soft delete) y lo registra en auditoría.
     */
    void deleteProduct(ProductId id);

/**
     * Crea una categoría.
     */
    Category createCategory(CategoryCommand command);

/**
     * Actualiza una categoría.
     */
    Category updateCategory(CategoryId id, CategoryCommand command);

/**
     * Activa una categoría.
     */
    Category activateCategory(CategoryId id);

/**
     * Desactiva una categoría y lo registra en auditoría.
     */
    Category deactivateCategory(CategoryId id);

/**
     * Elimina una categoría (soft delete) y lo registra en auditoría.
     */
    void deleteCategory(CategoryId id);

/**
     * Crea una marca.
     */
    Brand createBrand(BrandCommand command);

/**
     * Actualiza una marca.
     */
    Brand updateBrand(BrandId id, BrandCommand command);

/**
     * Activa una marca.
     */
    Brand activateBrand(BrandId id);

/**
     * Desactiva una marca y lo registra en auditoría.
     */
    Brand deactivateBrand(BrandId id);

/**
     * Elimina una marca (soft delete) y lo registra en auditoría.
     */
    void deleteBrand(BrandId id);
}