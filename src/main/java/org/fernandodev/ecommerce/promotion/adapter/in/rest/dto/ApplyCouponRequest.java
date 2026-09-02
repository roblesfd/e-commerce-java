package org.fernandodev.ecommerce.promotion.adapter.in.rest.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Petición para validar o aplicar un cupón sobre un carrito.
 */
public record ApplyCouponRequest(
        @NotBlank String code,
        List<@Valid CartItemRequest> items
) {

    public ApplyCouponRequest {
        items = items == null ? List.of() : items;
    }

    public record CartItemRequest(
            @NotNull UUID productId,
            @Positive int quantity,
            @NotNull MoneyRequest unitPrice
    ) {
    }

    public record MoneyRequest(
            @NotNull BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) String currency
    ) {
    }
}