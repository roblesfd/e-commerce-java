package org.fernandodev.ecommerce.customer.application.dto;

/**
 * Datos de preferencias del cliente.
 */
public record PreferencesCommand(
        boolean newsletterEnabled,
        String preferredCurrencyCode
) {}
