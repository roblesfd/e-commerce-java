package org.fernandodev.ecommerce.payment.adapter.in.rest;

import org.fernandodev.ecommerce.payment.adapter.in.rest.dto.WebhookRequest;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.shared.domain.exception.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Recibe los callbacks del proveedor de pagos (webhooks).
 * <p>
 * Autenticidad validada por un secreto compartido en el header
 * {@code X-Webhook-Signature}. El procesamiento es idempotente: callbacks
 * duplicados o tardíos no duplican capturas ni reembolsos.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/payments/webhooks")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private static final String SIGNATURE_HEADER = "X-Webhook-Signature";

    private final PaymentUseCases paymentUseCases;

    @Value("${payment.webhook-secret:mock-secret}")
    private String webhookSecret;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void process(@RequestHeader(SIGNATURE_HEADER) String signature,
                        @Valid @RequestBody WebhookRequest request) {
        if (!webhookSecret.equals(signature)) {
            throw new AuthenticationException("Firma de webhook inválida");
        }
        paymentUseCases.processWebhook(request.transactionId(), request.status());
    }
}