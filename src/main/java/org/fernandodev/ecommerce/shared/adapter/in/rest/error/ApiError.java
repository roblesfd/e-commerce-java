package org.fernandodev.ecommerce.shared.adapter.in.rest.error;

/**
 * Modelo de error estandarizado para todas las respuestas de error de la API
 * (spec §33). Sigue un formato compatible con RFC 7807.
 *
 * @param type          URI que identifica el tipo de error.
 * @param title         título legible del error.
 * @param status        código HTTP.
 * @param detail        detalle del error.
 * @param instance      recurso en el que ocurrió el error.
 * @param correlationId identificador de correlación de la petición.
 */
public record ApiError(
        String type,
        String title,
        int status,
        String detail,
        String instance,
        String correlationId
) {

    /**
     * Construye una respuesta de error.
     *
     * @param status        código HTTP.
     * @param title         título del error.
     * @param detail        detalle.
     * @param instance      URI de la petición.
     * @param correlationId identificador de correlación (opcional).
     * @return la respuesta de error.
     */
    public static ApiError of(int status, String title, String detail, String instance, String correlationId) {
        return new ApiError("about:blank", title, status, detail, instance, correlationId);
    }
}