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

    /**
     * Persiste el producto.
     *
     * @param product producto a guardar.
     * @return el producto persistido.
     */
    Product save(Product product);

    /**
     * @param id identificador del producto.
     * @return el producto, o {@code Optional.empty()} si no existe o está eliminado.
     */
    Optional<Product> findById(ProductId id);

    /**
     * @param sku SKU.
     * @return {@code true} si ya existe un producto no eliminado con ese SKU.
     */
    boolean existsBySku(Sku sku);

    /**
     * @param barcode código de barras.
     * @return {@code true} si ya existe un producto no eliminado con ese barcode.
     */
    boolean existsByBarcode(Barcode barcode);

    /**
     * @param brandId identificador de la marca.
     * @return {@code true} si existe algún producto no eliminado asociado a la marca.
     */
    boolean existsByBrandId(BrandId brandId);

    /**
     * @param categoryId identificador de la categoría.
     * @return {@code true} si existe algún producto no eliminado asociado a la categoría.
     */
    boolean existsByCategoryId(CategoryId categoryId);

    /**
     * Busca productos aplicando filtros y paginación.
     *
     * @param search filtros de búsqueda.
     * @param query  paginación y orden.
     * @return página de resultados.
     */
    PageResult<Product> search(ProductSearch search, PageQuery query);
}