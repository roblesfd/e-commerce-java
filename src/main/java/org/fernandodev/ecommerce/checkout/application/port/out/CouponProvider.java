package org.fernandodev.ecommerce.checkout.application.port.out;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.checkout.application.dto.CouponItemDto;
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
     * @param items      items del checkout.
     * @return el descuento calculado.
     */
    Money discountFor(UUID customerId, String code, List<CouponItemDto> items);
}