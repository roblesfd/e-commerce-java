package org.fernandodev.ecommerce.shipping.adapter.in.rest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Petición para crear un método de envío.
 */
public record ShippingMethodRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Size(max = 120) String carrier,
        @NotNull MoneyRequest cost
) {

    public record MoneyRequest(@NotNull BigDecimal amount, @NotBlank @Size(min = 3, max = 3) String currency) {
    }
}