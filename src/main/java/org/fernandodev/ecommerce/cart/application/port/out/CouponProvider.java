package org.fernandodev.ecommerce.cart.application.port.out;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.cart.application.dto.CouponItemDto;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Proveedor que valida y calcula el descuento de un cupón (contexto Promotions).
 */
public interface CouponProvider {

    /**
     * Valida el cupón y calcula su descuento sobre los items.
     *
     * @param customerId identificador del cliente.
     * @param code       código del cupón.
     * @param items      items del carrito para evaluar las promociones.
     * @return el descuento calculado.
     * @throws org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException
     *         si el cupón no es aplicable.
     */
    Money discountFor(UUID customerId, String code, List<CouponItemDto> items);
}