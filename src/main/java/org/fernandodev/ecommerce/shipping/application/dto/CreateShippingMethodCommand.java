package org.fernandodev.ecommerce.shipping.application.dto;

import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Datos para crear un método de envío.
 */
public record CreateShippingMethodCommand(String name, String carrier, Money cost) {
}