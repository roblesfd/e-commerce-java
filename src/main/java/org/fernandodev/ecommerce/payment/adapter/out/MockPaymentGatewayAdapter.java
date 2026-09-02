package org.fernandodev.ecommerce.payment.adapter.out;

import java.math.BigDecimal;

import org.fernandodev.ecommerce.payment.application.port.out.PaymentGateway;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.stereotype.Component;

/**
 * Mock de pasarela de pago.
 * <p>
 * Devuelve {@code SUCCESS} salvo que el importe coincida con un
 * {@code failAmount} ({@code FAILURE}) o un {@code unknownAmount}
 * ({@code UNKNOWN}) configurables, para probar el manejo de fallos y de
 * estados desconocidos. Los proveedores externos lo sustituirán con adapters.
 * </p>
 */
@Component
public class MockPaymentGatewayAdapter implements PaymentGateway {

    private volatile BigDecimal failAmount;
    private volatile BigDecimal unknownAmount;

    public void setFailAmount(BigDecimal failAmount) {
        this.failAmount = failAmount;
    }

    public void setUnknownAmount(BigDecimal unknownAmount) {
        this.unknownAmount = unknownAmount;
    }

    @Override
    public GatewayResult authorize(String transactionId, Money amount) {
        return new GatewayResult(transactionId, outcomeFor(amount));
    }

    @Override
    public GatewayResult capture(String transactionId, Money amount) {
        return new GatewayResult(transactionId, outcomeFor(amount));
    }

    @Override
    public GatewayResult refund(String transactionId, Money amount) {
        return new GatewayResult(transactionId, outcomeFor(amount));
    }

    @Override
    public GatewayResult cancel(String transactionId, Money amount) {
        return new GatewayResult(transactionId, outcomeFor(amount));
    }

    private GatewayOutcome outcomeFor(Money amount) {
        if (failAmount != null && amount.amount().compareTo(failAmount) == 0) {
            return GatewayOutcome.FAILURE;
        }
        if (unknownAmount != null && amount.amount().compareTo(unknownAmount) == 0) {
            return GatewayOutcome.UNKNOWN;
        }
        return GatewayOutcome.SUCCESS;
    }
}