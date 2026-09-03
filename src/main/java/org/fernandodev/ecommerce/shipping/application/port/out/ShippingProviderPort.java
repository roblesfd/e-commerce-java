package org.fernandodev.ecommerce.shipping.application.port.out;

/**
 * Puerta hacia el proveedor externo de envíos.
 * <p>
 * El mock adapter la implementa; los carriers reales la sustituirán.
 * </p>
 */
public interface ShippingProviderPort {

    String createShipment(String carrier, String addressSnapshot);
}