package org.fernandodev.ecommerce.pricing.application.dto;

import java.math.BigDecimal;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;

/**
 * Datos para definir el precio base de un producto o variante.
 *
 * @param productId identificador del producto.
 * @param variantId identificador de la variante (null para pricing a nivel producto).
 * @param amount    importe del precio base.
 * @param currency  divisa ISO 4217.
 */
public record DefinePriceCommand(
        ProductId productId,
        ProductVariantId variantId,
        BigDecimal amount,
        String currency
) {
}