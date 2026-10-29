
package org.fernandodev.ecommerce.shipping.application.port.out;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * Resultado de la consulta de seguimiento de un envío al proveedor externo.t
 *
 * <p>Contiene el identificador del envío, su número de seguimiento,
 * el estado comunicado por el proveedor y la fecha de la última actualización.</p>
 *
 * @param providerShipmentId identificador del envío en el proveedor.
 * @param trackingNumber número de seguimiento del paquete.
 * @param providerStatus estado informado por el proveedor.
 * @param lastUpdatedAt instante de la última actualización conocida.
 * @param trackingUrl URL de seguimiento proporcionada por el proveedor,
 *                    si está disponible.
 */
public record ShipmentTrackingResult(
        String providerShipmentId,
        String trackingNumber,
        String providerStatus,
        Instant lastUpdatedAt,
        String trackingUrl
) {

    public ShipmentTrackingResult {
        Objects.requireNonNull(
                providerShipmentId, "El identificador del envío es obligatorio");
        Objects.requireNonNull(
                trackingNumber, "El número de seguimiento es obligatorio");
        Objects.requireNonNull(
                providerStatus, "El estado del proveedor es obligatorio");
        Objects.requireNonNull(
                lastUpdatedAt, "La fecha de actualización es obligatoria");

        if (providerShipmentId.isBlank()
                || trackingNumber.isBlank()
                || providerStatus.isBlank()) {
            throw new IllegalArgumentException(
                    "Los identificadores y el estado no pueden estar vacíos");
        }

        trackingUrl = trackingUrl == null || trackingUrl.isBlank()
                ? null
                : trackingUrl;
    }

    /**
     * Devuelve la URL de seguimiento si el proveedor la proporciona.
     *
     * @return URL de seguimiento opcional.
     */
    public Optional<String> trackingUrlOptional() {
        return Optional.ofNullable(trackingUrl);
    }
}
