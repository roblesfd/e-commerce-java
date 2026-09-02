package org.fernandodev.ecommerce.pricing.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Petición para definir el precio base de un producto o variante.
 *
 * @param amount    importe del precio base.
 * @param currency  divisa ISO 4217.
 * @param variantId identificador de la variante (null para pricing a nivel producto).
 */
public record DefinePriceRequest(
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        UUID variantId
) {
}