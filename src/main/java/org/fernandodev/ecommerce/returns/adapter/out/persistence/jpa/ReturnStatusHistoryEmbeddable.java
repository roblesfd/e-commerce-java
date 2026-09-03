package org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa;

import java.time.Instant;

import org.fernandodev.ecommerce.returns.domain.value_object.ReturnStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con una entrada del historial de estados de la devolución.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ReturnStatusHistoryEmbeddable {

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReturnStatus status;

    @Column(name = "at", nullable = false)
    private Instant at;
}