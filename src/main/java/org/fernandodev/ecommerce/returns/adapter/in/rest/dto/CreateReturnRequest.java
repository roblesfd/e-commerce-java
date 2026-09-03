package org.fernandodev.ecommerce.returns.adapter.in.rest.dto;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.returns.domain.value_object.ReturnReason;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Petición para crear una solicitud de devolución.
 */
public record CreateReturnRequest(
        @NotNull UUID orderId,
        @NotNull ReturnReason reason,
        @NotEmpty @Valid List<ItemRequest> items
) {

    public record ItemRequest(@NotNull UUID orderItemId, int quantity) {
    }
}