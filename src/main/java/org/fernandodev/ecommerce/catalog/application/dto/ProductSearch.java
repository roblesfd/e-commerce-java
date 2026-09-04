package org.fernandodev.ecommerce.catalog.application.dto;

import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;

/**
 * Filtros de búsqueda de productos.
 *
 * @param active     estado activo/inactivo (null = sin filtrar).
 * @param categoryId categoría (null = sin filtrar).
 * @param brandId    marca (null = sin filtrar).
 * @param query      búsqueda textual por nombre/SKU (null = sin filtrar).
 */
public record ProductSearch(Boolean active, CategoryId categoryId, BrandId brandId, String query) {

    /**
     * @return una búsqueda sin filtros.
     */
    public static ProductSearch all() {
        return new ProductSearch(null, null, null, null);
    }
}