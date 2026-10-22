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
}