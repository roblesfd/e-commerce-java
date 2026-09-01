package org.fernandodev.ecommerce.customer.application.dto;

/**
 * Datos de una dirección para su creación o actualización.
 */
public record AddressCommand(String street, String city, String state, String postalCode, String country) {
}