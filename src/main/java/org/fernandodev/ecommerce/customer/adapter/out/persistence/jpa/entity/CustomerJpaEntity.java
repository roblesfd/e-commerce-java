package org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.fernandodev.ecommerce.customer.adapter.out.persistence.jpa.CustomerAddressEmbeddable;

/**
 * Entidad JPA del agregado {@code customer}.
 */
@Entity
@Table(name = "customer")
@Getter
@Setter
@NoArgsConstructor
public class CustomerJpaEntity {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "iam_user_id", nullable = false, unique = true)
    private UUID iamUserId;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "default_address_id")
    private UUID defaultAddressId;

    @Column(name = "newsletter_enabled", nullable = false)
    private boolean newsletterEnabled;

    @Column(name = "preferred_currency", nullable = false, length = 3)
    private String preferredCurrency;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "customer_address", joinColumns = @JoinColumn(name = "customer_id"))
    @OrderColumn(name = "position")
    private List<CustomerAddressEmbeddable> addresses = new ArrayList<>();
}
