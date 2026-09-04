package org.fernandodev.ecommerce.shipping.adapter.in.rest;

import org.fernandodev.ecommerce.shipping.adapter.in.rest.dto.ShipmentWebhookRequest;
import org.fernandodev.ecommerce.shipping.application.port.in.ShipmentUseCases;
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
 * Recibe los callbacks del proveedor de envíos (webhooks).
 * <p>
 * Autenticidad validada por un secreto compartido en el header
 * {@code X-Webhook-Signature}. El procesamiento es idempotente.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/shipments/webhooks")
@RequiredArgsConstructor
public class ShipmentWebhookController {

    private static final String SIGNATURE_HEADER = "X-Webhook-Signature";

    private final ShipmentUseCases shipmentUseCases;

    @Value("${shipping.webhook-secret:mock-secret}")
    private String webhookSecret;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Process.
     * @param signature signature.
     * @param request request.
     */
    public void process(@RequestHeader(SIGNATURE_HEADER) String signature,
                        @Valid @RequestBody ShipmentWebhookRequest request) {
        if (!webhookSecret.equals(signature)) {
            throw new AuthenticationException("Firma de webhook inválida");
        }
        shipmentUseCases.processWebhook(request.trackingNumber(), request.status());
    }
}