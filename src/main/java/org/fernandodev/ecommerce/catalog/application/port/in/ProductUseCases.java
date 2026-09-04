package org.fernandodev.ecommerce.catalog.application.port.in;

import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.dto.PageResult;
import org.fernandodev.ecommerce.catalog.application.dto.ProductCommand;
import org.fernandodev.ecommerce.catalog.application.dto.ProductSearch;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;

/**
 * Casos de uso del contexto Product Catalog (puerto de entrada) para productos.
 */
public interface ProductUseCases {

    /**
     * Crea un producto activo.
     *
     * @param command datos del producto.
     * @return el producto creado.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ConflictException
     *         si el SKU o el barcode ya existen.
     */
    Product create(ProductCommand command);

    /**
     * @param id identificador del producto.
     * @return el producto.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException
     *         si no existe o está eliminado.
     */
    Product getById(ProductId id);

    /**
     * Busca productos aplicando filtros y paginación.
     *
     * @param search filtros de búsqueda.
     * @param query  paginación y orden.
     * @return página de resultados.
     */
    PageResult<Product> list(ProductSearch search, PageQuery query);

    /**
     * Actualiza el producto (semántica PUT: reemplazo completo de colecciones).
     *
     * @param id      identificador del producto.
     * @param command nuevos datos del producto.
     * @return el producto actualizado.
     */
    Product update(ProductId id, ProductCommand command);

    /**
     * Activa el producto.
     *
     * @param id identificador del producto.
     * @return el producto activado.
     */
    Product activate(ProductId id);

    /**
     * Desactiva el producto.
     *
     * @param id identificador del producto.
     * @return el producto desactivado.
     */
    Product deactivate(ProductId id);

    /**
     * Elimina el producto (soft delete).
     *
     * @param id identificador del producto.
     */
    void delete(ProductId id);
}
