package org.fernandodev.ecommerce.checkout.application.dto;

/**
 * Información del producto (snapshot para la orden).
 */
public record ProductInfo(String sku, String name) {
}