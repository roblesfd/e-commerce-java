package org.fernandodev.ecommerce.shipping.application.port.out;


import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/**
 * Resultado de una solicitud de cotización de envío.
 *
 * <p>Contiene las opciones logísticas devueltas por el proveedor,
 * incluyendo transportista, servicio, precio y tiempo estimado de entrega.</p>
 *
 * @param quotes opciones de envío disponibles.
 */
public record ShippingQuoteResult(List<ShippingOption> quotes) {

    public ShippingQuoteResult {
        Objects.requireNonNull(quotes, "La lista de cotizaciones es obligatoria");
        quotes = List.copyOf(quotes);
    }

    /**
     * Representa una alternativa de envío ofrecida por el proveedor.
     *
     * @param carrier nombre del transportista.
     * @param serviceName nombre comercial del servicio de envío.
     * @param price precio de la opción de envío.
     * @param currency código de moneda del precio, por ejemplo MXN.
     * @param estimatedDelivery tiempo estimado de entrega expresado
     *                          como una duración.
     */
    public record ShippingOption(
            String carrier,
            String serviceName,
            BigDecimal price,
            String currency,
            Duration estimatedDelivery
    ) {

        public ShippingOption {
            Objects.requireNonNull(carrier, "El transportista es obligatorio");
            Objects.requireNonNull(serviceName, "El servicio es obligatorio");
            Objects.requireNonNull(price, "El precio es obligatorio");
            Objects.requireNonNull(currency, "La moneda es obligatoria");
            Objects.requireNonNull(
                    estimatedDelivery, "El tiempo estimado de entrega es obligatorio");

            if (carrier.isBlank() || serviceName.isBlank() || currency.isBlank()) {
                throw new IllegalArgumentException(
                        "El transportista, servicio y moneda no pueden estar vacíos");
            }

            if (price.signum() < 0) {
                throw new IllegalArgumentException("El precio no puede ser negativo");
            }

            if (estimatedDelivery.isNegative() || estimatedDelivery.isZero()) {
                throw new IllegalArgumentException(
                        "El tiempo estimado debe ser mayor que cero");
            }
        }
    }
}
