package org.fernandodev.ecommerce.catalog.application.dto;

/**
 * Parámetros de paginación y ordenación de una consulta.
 *
 * @param page          índice de página (base 0, se normaliza a ≥ 0).
 * @param size          tamaño de página (se normaliza a 1..100).
 * @param sortBy        campo de ordenación (por defecto {@code name}).
 * @param sortDirection dirección de ordenación ({@code asc} o {@code desc}).
 */
public record PageQuery(int page, int size, String sortBy, String sortDirection) {

    /**
     * Constructor compacto que normaliza página, tamaño y ordenación.
     */
    public PageQuery {
        page = Math.max(page, 0);
        size = Math.min(Math.max(size, 1), 100);
        sortBy = sortBy == null || sortBy.isBlank() ? "name" : sortBy;
        sortDirection = sortDirection == null || sortDirection.isBlank() ? "asc" : sortDirection;
    }

    /**
     * Crea una consulta paginada con ordenación por nombre ascendente.
     *
     * @param page índice de página (base 0).
     * @param size tamaño de página.
     * @return la consulta paginada.
     */
    public static PageQuery of(int page, int size) {
        return new PageQuery(page, size, "name", "asc");
    }
}