package org.fernandodev.ecommerce.notifications.application.dto;

/**
 * Datos para actualizar una plantilla.
 */
public record UpdateTemplateCommand(String title, String body) {
}