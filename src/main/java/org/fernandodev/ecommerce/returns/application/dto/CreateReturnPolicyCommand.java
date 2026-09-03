package org.fernandodev.ecommerce.returns.application.dto;

import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;

/**
 * Datos para crear una política de devoluciones.
 */
public record CreateReturnPolicyCommand(int returnWindowDays, Set<ProductId> returnableProductIds) {

    public CreateReturnPolicyCommand {
        returnableProductIds = returnableProductIds == null ? Set.of() : returnableProductIds;
    }
}