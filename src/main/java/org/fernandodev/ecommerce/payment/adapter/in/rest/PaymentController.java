package org.fernandodev.ecommerce.payment.adapter.in.rest;

import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.adapter.in.rest.dto.InitiatePaymentRequest;
import org.fernandodev.ecommerce.payment.adapter.in.rest.dto.PaymentResponse;
import org.fernandodev.ecommerce.payment.adapter.in.rest.dto.RefundRequest;
import org.fernandodev.ecommerce.payment.adapter.in.rest.mapper.PaymentMapper;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST del contexto Payments.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentUseCases paymentUseCases;
    private final PaymentMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse initiate(@RequestHeader("Idempotency-Key") String idempotencyKey,
                                    @Valid @RequestBody InitiatePaymentRequest request) {
        return mapper.toResponse(paymentUseCases.initiate(
                OrderId.of(request.orderId()),
                Money.of(request.amount(), request.currency()),
                idempotencyKey));
    }

    @GetMapping("/{id}")
    /**
     * Get.
     * @param id id.
     */
    public PaymentResponse get(@PathVariable UUID id) {
        return mapper.toResponse(paymentUseCases.getById(PaymentId.of(id)));
    }

    @GetMapping
    /**
     * Get by order.
     * @param orderId orderId.
     */
    public PaymentResponse getByOrder(@RequestParam UUID orderId) {
        return paymentUseCases.getByOrder(OrderId.of(orderId))
                .map(mapper::toResponse)
                .orElseThrow(() -> new org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException(
                        "Pago no encontrado para la orden: " + orderId));
    }

    @PostMapping("/{id}/capture")
    /**
     * Capture.
     * @param id id.
     */
    public PaymentResponse capture(@PathVariable UUID id) {
        return mapper.toResponse(paymentUseCases.capture(PaymentId.of(id)));
    }

    @PostMapping("/{id}/reject")
    /**
     * Reject.
     * @param id id.
     */
    public PaymentResponse reject(@PathVariable UUID id) {
        return mapper.toResponse(paymentUseCases.reject(PaymentId.of(id)));
    }

    @PostMapping("/{id}/cancel")
    /**
     * Cancel.
     * @param id id.
     */
    public PaymentResponse cancel(@PathVariable UUID id) {
        return mapper.toResponse(paymentUseCases.cancel(PaymentId.of(id)));
    }

    @PostMapping("/{id}/refund")
    /**
     * Refund.
     * @param id id.
     * @param request request.
     */
    public PaymentResponse refund(@PathVariable UUID id, @Valid @RequestBody RefundRequest request) {
        return mapper.toResponse(paymentUseCases.refund(
                PaymentId.of(id), Money.of(request.amount(), request.currency())));
    }
}