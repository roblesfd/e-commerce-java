package org.fernandodev.ecommerce.returns.adapter.in.rest.mapper;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.CreateReturnPolicyRequest;
import org.fernandodev.ecommerce.returns.adapter.in.rest.dto.ReturnPolicyResponse;
import org.fernandodev.ecommerce.returns.application.dto.CreateReturnPolicyCommand;
import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.springframework.stereotype.Component;

/**
 * Mapeo entre el agregado {@link ReturnPolicy} y sus DTOs REST.
 */
@Component
public class ReturnPolicyMapper {

    public CreateReturnPolicyCommand toCommand(CreateReturnPolicyRequest request) {
        Set<ProductId> products = request.returnableProductIds() == null
                ? Set.of()
                : request.returnableProductIds().stream().map(ProductId::of).collect(Collectors.toSet());
        return new CreateReturnPolicyCommand(request.returnWindowDays(), products);
    }

    public ReturnPolicyResponse toResponse(ReturnPolicy policy) {
        Set<UUID> products = policy.getReturnableProductIds().stream()
                .map(ProductId::value)
                .collect(Collectors.toSet());
        return new ReturnPolicyResponse(policy.getId().value(), policy.getReturnWindowDays(),
                policy.isActive(), products);
    }
}