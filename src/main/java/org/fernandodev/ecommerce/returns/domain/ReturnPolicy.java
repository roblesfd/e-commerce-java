package org.fernandodev.ecommerce.returns.domain;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz que representa una política de devoluciones configurable
 * (ventana en días y productos reembolsables).
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReturnPolicy {

    private ReturnPolicyId id;
    private int returnWindowDays;
    private boolean active;
    @Getter(AccessLevel.NONE)
    private Set<ProductId> returnableProductIds;

    public static ReturnPolicy create(ReturnPolicyId id, int returnWindowDays, Set<ProductId> returnableProductIds) {
        return crear(id, returnWindowDays, true, returnableProductIds);
    }

    public static ReturnPolicy crear(ReturnPolicyId id, int returnWindowDays, boolean active,
                                     Set<ProductId> returnableProductIds) {
        ReturnPolicy policy = new ReturnPolicy();
        policy.id = Objects.requireNonNull(id, "id no puede ser null");
        if (returnWindowDays <= 0) {
            throw new BusinessRuleViolationException("returnWindowDays debe ser positivo");
        }
        policy.returnWindowDays = returnWindowDays;
        policy.active = active;
        policy.returnableProductIds = new HashSet<>(returnableProductIds == null ? Set.of() : returnableProductIds);
        return policy;
    }

    public Set<ProductId> getReturnableProductIds() {
        return java.util.Collections.unmodifiableSet(returnableProductIds);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    /**
     * Un producto es reembolsable si la política no restringe productos o si
     * está en la lista.
     */
    public boolean isProductReturnable(ProductId productId) {
        return returnableProductIds.isEmpty() || returnableProductIds.contains(productId);
    }
}