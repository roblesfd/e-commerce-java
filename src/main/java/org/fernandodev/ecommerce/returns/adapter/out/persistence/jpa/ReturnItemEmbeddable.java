package org.fernandodev.ecommerce.returns.adapter.out.persistence.jpa;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Embeddable con un item devuelto.
 */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class ReturnItemEmbeddable {

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "refund_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
}