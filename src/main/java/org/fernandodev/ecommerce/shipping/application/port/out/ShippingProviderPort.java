package org.fernandodev.ecommerce.shipping.application.port.out;

/**
 * Puerta hacia el proveedor externo de envíos.
 * <p>
 * El mock adapter la implementa; los carriers reales la sustituirán.
 * </p>
 */
public interface ShippingProviderPort {

    /**
     * Solicita al proveedor la creación de un envío y devuelve su tracking.
     *
     * @param carrier          transportista (p. ej. {@code DHL}).
     * @param addressSnapshot  snapshot de la dirección de entrega.
     * @return el número de tracking del envío creado.
     * @throws IllegalStateException si el proveedor no confirma el envío.
     */
    String createShipment(String carrier, String addressSnapshot);


    /**
     * Solicita una cotización de envío al proveedor externo.
     *
     * @param request datos del paquete y de las direcciones necesarias
     *                para calcular las opciones de envío.
     * @return cotización con las opciones de envío disponibles.
     */
    ShippingQuoteResult quote(ShippingQuoteRequest request);

    /**
     * Consulta al proveedor el estado actual de un envío.
     *
     * @param providerShipmentId identificador del envío en el sistema
     *                           del proveedor externo.
     * @return información de seguimiento proporcionada por el proveedor.
     */
    ShipmentTrackingResult trackShipment(String providerShipmentId);

    /**
     * Solicita al proveedor la cancelación de un envío.
     *
     * @param providerShipmentId identificador del envío en el sistema
     *                           del proveedor externo.
     */
    void cancelShipment(String providerShipmentId);
}
