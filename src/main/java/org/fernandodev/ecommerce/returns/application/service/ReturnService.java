package org.fernandodev.ecommerce.returns.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.application.dto.CreateReturnCommand;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnUseCases;
import org.fernandodev.ecommerce.returns.application.port.out.OrderPort;
import org.fernandodev.ecommerce.returns.application.port.out.PaymentPort;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnPolicyRepository;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnRepository;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.fernandodev.ecommerce.returns.domain.value_object.InspectionResult;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnItem;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación de devoluciones.
 * <p>
 * Valida la elegibilidad (orden entregada, ventana de devolución, cantidades,
 * productos reembolsables, sin retorno previo) y orquesta el reembolso al
 * completar el retorno a través del contexto Payment.
 * </p>
 */
@RequiredArgsConstructor
public class ReturnService implements ReturnUseCases {

    private static final String DELIVERED = "DELIVERED";

    private final ReturnRepository returns;
    private final ReturnPolicyRepository policies;
    private final OrderPort orders;
    private final PaymentPort payments;

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return create(OrderId orderId, CreateReturnCommand command) {
        if (returns.existsByOrderId(orderId)) {
            throw new ConflictException("Ya existe una devolución para la orden: " + orderId);
        }
        OrderPort.OrderInfo order = orders.getOrder(orderId);
        if (!DELIVERED.equals(order.status())) {
            throw new BusinessRuleViolationException("La orden debe estar entregada para devolverse");
        }
        ReturnPolicy policy = policies.findActive()
                .orElseThrow(() -> new BusinessRuleViolationException("No hay una política de devoluciones activa"));
        Instant now = Instant.now();
        if (!order.deliveredAt().plus(java.time.Duration.ofDays(policy.getReturnWindowDays())).isAfter(now)) {
            throw new BusinessRuleViolationException("La ventana de devolución ha expirado");
        }
        if (command.items().isEmpty()) {
            throw new BusinessRuleViolationException("La devolución requiere al menos un item");
        }

        List<ReturnItem> items = command.items().stream()
                .map(itemCommand -> toReturnItem(order, policy, itemCommand))
                .toList();
        Return aReturn = Return.create(ReturnId.generate(), orderId, order.customerId(),
                command.reason(), items, now);
        return returns.save(aReturn);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Return getById(ReturnId id) {
        return getExisting(id);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public List<Return> listByCustomer(UUID customerId) {
        return returns.findByCustomerId(customerId);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public Optional<Return> getByOrder(OrderId orderId) {
        return returns.findByOrderId(orderId);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    public List<Return> listAll() {
        return returns.findAll();
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return approve(ReturnId id) {
        Return aReturn = getExisting(id);
        Money refundAmount = aReturn.getItems().stream()
                .map(ReturnItem::refundAmount)
                .reduce(Money::add)
                .orElseThrow();
        aReturn.approve(refundAmount, Instant.now());
        return returns.save(aReturn);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return reject(ReturnId id) {
        Return aReturn = getExisting(id);
        aReturn.reject(Instant.now());
        return returns.save(aReturn);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return cancel(ReturnId id) {
        Return aReturn = getExisting(id);
        aReturn.cancel(Instant.now());
        return returns.save(aReturn);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return registerReturnShipment(ReturnId id, String trackingNumber) {
        Return aReturn = getExisting(id);
        aReturn.registerReturnShipment(trackingNumber, Instant.now());
        return returns.save(aReturn);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return receive(ReturnId id) {
        Return aReturn = getExisting(id);
        aReturn.receive(Instant.now());
        return returns.save(aReturn);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return inspect(ReturnId id, String condition, String notes, boolean passed) {
        Return aReturn = getExisting(id);
        aReturn.inspect(new InspectionResult(condition, notes, passed, Instant.now()), Instant.now());
        return returns.save(aReturn);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return complete(ReturnId id) {
        Return aReturn = getExisting(id);
        aReturn.complete(Instant.now());
        if (!aReturn.isRefundRequested()) {
            requestRefund(aReturn);
        }
        return returns.save(aReturn);
    }

    /**

     * {@inheritDoc}

     */

    @Override
    @Transactional
    public Return retryRefund(ReturnId id) {
        Return aReturn = getExisting(id);
        if (!aReturn.canRetryRefund()) {
            throw new BusinessRuleViolationException("El reembolso no es reintentable");
        }
        requestRefund(aReturn);
        return returns.save(aReturn);
    }

    private void requestRefund(Return aReturn) {
        aReturn.markRefundRequested(Instant.now());
        boolean ok = payments.refund(aReturn.getOrderId(), aReturn.getRefundAmount());
        if (ok) {
            aReturn.markRefundSucceeded(Instant.now());
        } else {
            aReturn.markRefundFailed(Instant.now());
        }
    }

    private ReturnItem toReturnItem(OrderPort.OrderInfo order, ReturnPolicy policy,
                                    CreateReturnCommand.ReturnItemCommand command) {
        OrderPort.ItemInfo orderItem = order.items().stream()
                .filter(item -> item.orderItemId().equals(command.orderItemId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Item de orden no encontrado: " + command.orderItemId()));
        if (command.quantity() > orderItem.quantity()) {
            throw new BusinessRuleViolationException(
                    "Cantidad devuelta mayor a la comprada: " + command.orderItemId());
        }
        if (!policy.isProductReturnable(orderItem.productId())) {
            throw new BusinessRuleViolationException(
                    "El producto no es reembolsable: " + orderItem.productId());
        }
        Money refundAmount = orderItem.unitPrice()
                .multiply(java.math.BigDecimal.valueOf(command.quantity()));
        return new ReturnItem(orderItem.orderItemId(), orderItem.productId(), command.quantity(), refundAmount);
    }

    private Return getExisting(ReturnId id) {
        return returns.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Devolución no encontrada: " + id));
    }
}