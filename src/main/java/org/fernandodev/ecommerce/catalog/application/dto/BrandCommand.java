package org.fernandodev.ecommerce.catalog.application.dto;

/**
 * Datos de creación o actualización de una marca.
 *
 * @param name nombre de la marca (obligatorio).
 */
public record BrandCommand(String name) {
}