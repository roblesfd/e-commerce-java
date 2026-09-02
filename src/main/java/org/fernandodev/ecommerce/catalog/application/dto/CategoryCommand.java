package org.fernandodev.ecommerce.catalog.application.dto;

import java.util.UUID;

/**
 * Datos de creación o actualización de una categoría.
 *
 * @param name     nombre de la categoría (obligatorio).
 * @param parentId identificador de la categoría padre (opcional, jerarquía).
 */
public record CategoryCommand(String name, UUID parentId) {
}