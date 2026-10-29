package org.fernandodev.ecommerce.shipping.application.port.out;


import java.math.BigDecimal;
import java.util.Objects;

/**
 * Datos necesarios para solicitar una cotización de envío a un proveedor.
 *
 * <p>Incluye los códigos postales de origen y destino, así como el peso
 * y las dimensiones del paquete.</p>
 *
 * @param originPostalCode código postal de origen.
 * @param destinationPostalCode código postal de destino.
 * @param weightKg peso del paquete en kilogramos.
 * @param lengthCm longitud del paquete en centímetros.
 * @param widthCm ancho del paquete en centímetros.
 * @param heightCm altura del paquete en centímetros.
 * @param declaredValue valor declarado del contenido.
 * @param currency código de moneda del valor declarado, por ejemplo MXN.
 */
public record ShippingQuoteRequest(
        String originPostalCode,
        String destinationPostalCode,
        BigDecimal weightKg,
        BigDecimal lengthCm,
        BigDecimal widthCm,
        BigDecimal heightCm,
        BigDecimal declaredValue,
        String currency
) {

    public ShippingQuoteRequest {
        Objects.requireNonNull(originPostalCode, "El código postal de origen es obligatorio");
        Objects.requireNonNull(destinationPostalCode, "El código postal de destino es obligatorio");
        Objects.requireNonNull(weightKg, "El peso es obligatorio");
        Objects.requireNonNull(lengthCm, "La longitud es obligatoria");
        Objects.requireNonNull(widthCm, "El ancho es obligatorio");
        Objects.requireNonNull(heightCm, "La altura es obligatoria");
        Objects.requireNonNull(declaredValue, "El valor declarado es obligatorio");
        Objects.requireNonNull(currency, "La moneda es obligatoria");

        if (originPostalCode.isBlank() || destinationPostalCode.isBlank()) {
            throw new IllegalArgumentException("Los códigos postales no pueden estar vacíos");
        }

        if (weightKg.signum() <= 0
                || lengthCm.signum() <= 0
                || widthCm.signum() <= 0
                || heightCm.signum() <= 0) {
            throw new IllegalArgumentException(
                    "El peso y las dimensiones deben ser mayores que cero");
        }

        if (declaredValue.signum() < 0) {
            throw new IllegalArgumentException(
                    "El valor declarado no puede ser negativo");
        }

        if (currency.isBlank()) {
            throw new IllegalArgumentException("La moneda no puede estar vacía");
        }
    }
}
