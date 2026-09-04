package org.fernandodev.ecommerce.catalog.application.dto;

import java.util.List;

/**
 * Resultado paginado de una consulta.
 *
 * @param content       elementos de la página actual.
 * @param page          índice de página (base 0).
 * @param size          tamaño de página.
 * @param totalElements número total de elementos.
 * @param totalPages    número total de páginas.
 */
public record PageResult<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * Construye un resultado paginado calculando el número total de páginas.
     *
     * @param content       elementos de la página.
     * @param page          índice de página.
     * @param size          tamaño de página.
     * @param totalElements número total de elementos.
     * @return el resultado paginado.
     */
    public static <T> PageResult<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalElements / size);
        return new PageResult<>(content, page, size, totalElements, totalPages);
    }

    /**
     * @param page índice de página.
     * @param size tamaño de página.
     * @return un resultado paginado vacío.
     */
    public static <T> PageResult<T> empty(int page, int size) {
        return new PageResult<>(List.of(), page, size, 0, 0);
    }
}