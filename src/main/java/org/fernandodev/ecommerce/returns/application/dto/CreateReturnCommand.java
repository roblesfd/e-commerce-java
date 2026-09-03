package org.fernandodev.ecommerce.returns.application.dto;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.returns.domain.value_object.ReturnReason;

/**
 * Datos para crear una solicitud de devolución.
 */
public record CreateReturnCommand(ReturnReason reason, List<ReturnItemCommand> items) {

    public CreateReturnCommand {
        items = items == null ? List.of() : items;
    }

    public record ReturnItemCommand(UUID orderItemId, int quantity) {
    }
}