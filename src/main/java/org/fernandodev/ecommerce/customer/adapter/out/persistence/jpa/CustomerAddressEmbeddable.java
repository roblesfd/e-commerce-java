package org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dirección persistida como colección embebida del agregado {@code customer}.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class CustomerAddressEmbeddable {

    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "street", nullable = false)
    private String street;

    @Column(name = "city", nullable = false)
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @Column(name = "country", nullable = false)
    private String country;
}
