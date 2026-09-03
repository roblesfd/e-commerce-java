package org.fernandodev.ecommerce.search.application.dto;

import java.util.List;

/**
 * Resultado paginado de una búsqueda.
 */
public record SearchResults<T>(List<T> items, long total, int page, int size) {
}