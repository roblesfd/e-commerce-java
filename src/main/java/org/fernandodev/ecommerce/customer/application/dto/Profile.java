package org.fernandodev.ecommerce.customer.application.dto;

/**
 * Datos de perfil del cliente para creación y actualización.
 *
 * @param firstName     nombre.
 * @param lastName      apellido.
 * @param email         correo electrónico.
 * @param phoneNumber   teléfono (opcional).
 */
public record Profile(String firstName, String lastName, String email, String phoneNumber) {
}