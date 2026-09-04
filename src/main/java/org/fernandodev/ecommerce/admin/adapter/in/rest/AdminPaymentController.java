package org.fernandodev.ecommerce.admin.adapter.in.rest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminPaymentUseCases;
import org.fernandodev.ecommerce.admin.security.AdminRoles;
import org.fernandodev.ecommerce.admin.security.RequireGroup;
import org.fernandodev.ecommerce.admin.security.RequireRole;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de administración de pagos y reembolsos.
 */
@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final AdminPaymentUseCases adminPayment;

    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping
    public List<Payment> list() {
        return adminPayment.listAllPayments();
    }

    /**
     * @param id identificador del pago.
     * @return el pago y su estado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @GetMapping("/{id}")
    public Payment get(@PathVariable UUID id) {
        return adminPayment.getPayment(PaymentId.of(id));
    }

    /**
     * Reembolsa un pago por un monto.
     *
     * @param id      identificador del pago.
     * @param request monto y divisa del reembolso.
     * @return el pago reembolsado.
     */
    @RequireRole(AdminRoles.ADMIN_ROLE)
    @RequireGroup(AdminRoles.ADMIN_GROUP)
    @PostMapping("/{id}/refund")
    public Payment refund(@PathVariable UUID id, @Valid @RequestBody RefundRequest request) {
        return adminPayment.refundPayment(PaymentId.of(id), Money.of(request.amount(), request.currency()));
    }

    /**
     * Petición para reembolsar un pago.
     *
     * @param amount   monto a reembolsar.
     * @param currency divisa ISO 4217.
     */
    public record RefundRequest(
            @NotNull @Positive BigDecimal amount,
            @NotBlank @Size(min = 3, max = 3) String currency
    ) {
    }
}