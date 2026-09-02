package org.fernandodev.ecommerce.checkout.application.port.out;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.checkout.application.dto.CouponItemDto;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Proveedor que valida y calcula el descuento de un cupón (contexto Promotions).
 */
public interface CouponProvider {

    Money discountFor(UUID customerId, String code, List<CouponItemDto> items);
}