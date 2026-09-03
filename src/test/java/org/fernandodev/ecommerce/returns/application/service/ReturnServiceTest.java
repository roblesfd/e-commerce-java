package org.fernandodev.ecommerce.returns.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.returns.application.dto.CreateReturnCommand;
import org.fernandodev.ecommerce.returns.application.port.out.OrderPort;
import org.fernandodev.ecommerce.returns.application.port.out.PaymentPort;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnPolicyRepository;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnRepository;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.returns.domain.ReturnPolicy;
import org.fernandodev.ecommerce.returns.domain.value_object.RefundStatus;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnId;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnPolicyId;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnReason;
import org.fernandodev.ecommerce.returns.domain.value_object.ReturnStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

class ReturnServiceTest {

    private final FakeReturnRepository returns = new FakeReturnRepository();
    private final FakeReturnPolicyRepository policies = new FakeReturnPolicyRepository();
    private final FakeOrderPort orders = new FakeOrderPort();
    private final FakePaymentPort payments = new FakePaymentPort();
    private final ReturnService service = new ReturnService(returns, policies, orders, payments);

    private UUID orderItemId;
    private ProductId productId;
    private UUID orderId;

    @Test
    void createCreaDevolucionConRefundCalculado() {
        prepararOrdenEntregada();

        Return aReturn = service.create(OrderId.of(orderId),
                new CreateReturnCommand(ReturnReason.DEFECTIVE, List.of(
                        new CreateReturnCommand.ReturnItemCommand(orderItemId, 1))));

        assertThat(aReturn.getStatus()).isEqualTo(ReturnStatus.REQUESTED);
        assertThat(aReturn.getItems()).hasSize(1);
        assertThat(aReturn.getItems().get(0).refundAmount()).isEqualTo(usd(900));
        assertThat(returns.findById(aReturn.getId())).contains(aReturn);
    }

    @Test
    void createConOrdenNoEntregadaLanzaViolacion() {
        prepararOrdenEntregada();
        orders.status = "CONFIRMED";

        assertThatThrownBy(() -> service.create(OrderId.of(orderId),
                new CreateReturnCommand(ReturnReason.DEFECTIVE, List.of(
                        new CreateReturnCommand.ReturnItemCommand(orderItemId, 1)))))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void createFueraDeVentanaLanzaViolacion() {
        prepararOrdenEntregada();
        orders.deliveredAt = Instant.now().minus(Duration.ofDays(31));

        assertThatThrownBy(() -> service.create(OrderId.of(orderId),
                new CreateReturnCommand(ReturnReason.DEFECTIVE, List.of(
                        new CreateReturnCommand.ReturnItemCommand(orderItemId, 1)))))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void createConItemInexistenteLanzaNotFound() {
        prepararOrdenEntregada();

        assertThatThrownBy(() -> service.create(OrderId.of(orderId),
                new CreateReturnCommand(ReturnReason.DEFECTIVE, List.of(
                        new CreateReturnCommand.ReturnItemCommand(UUID.randomUUID(), 1)))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createConCantidadMayorALaCompradaLanzaViolacion() {
        prepararOrdenEntregada();

        assertThatThrownBy(() -> service.create(OrderId.of(orderId),
                new CreateReturnCommand(ReturnReason.DEFECTIVE, List.of(
                        new CreateReturnCommand.ReturnItemCommand(orderItemId, 5)))))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void createConProductoNoReembolsableLanzaViolacion() {
        prepararOrdenEntregada();
        policies.policy = ReturnPolicy.create(ReturnPolicyId.generate(), 30,
                java.util.Set.of(ProductId.generate()));

        assertThatThrownBy(() -> service.create(OrderId.of(orderId),
                new CreateReturnCommand(ReturnReason.DEFECTIVE, List.of(
                        new CreateReturnCommand.ReturnItemCommand(orderItemId, 1)))))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void createDuplicadoParaLaOrdenLanzaConflicto() {
        prepararOrdenEntregada();
        returns.existing = true;

        assertThatThrownBy(() -> service.create(OrderId.of(orderId),
                new CreateReturnCommand(ReturnReason.DEFECTIVE, List.of(
                        new CreateReturnCommand.ReturnItemCommand(orderItemId, 1)))))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void approveCalculaElRefundTotal() {
        Return aReturn = crearDevolucion();

        Return approved = service.approve(aReturn.getId());

        assertThat(approved.getStatus()).isEqualTo(ReturnStatus.APPROVED);
        assertThat(approved.getRefundAmount()).isEqualTo(usd(900));
    }

    @Test
    void cicloCompletoSolicitaRefundAlPayment() {
        Return aReturn = crearDevolucionCompletada();

        assertThat(aReturn.getStatus()).isEqualTo(ReturnStatus.COMPLETED);
        assertThat(aReturn.getRefundStatus()).isEqualTo(RefundStatus.REFUNDED);
        assertThat(aReturn.isRefundRequested()).isTrue();
        assertThat(payments.refundCalls).isEqualTo(1);
    }

    @Test
    void completeConRefundFallidoMarcaFailedYReintentable() {
        payments.refundResult = false;
        Return aReturn = crearDevolucionCompletada();

        assertThat(aReturn.getRefundStatus()).isEqualTo(RefundStatus.FAILED);
        assertThat(aReturn.canRetryRefund()).isTrue();

        payments.refundResult = true;
        Return retried = service.retryRefund(aReturn.getId());

        assertThat(retried.getRefundStatus()).isEqualTo(RefundStatus.REFUNDED);
    }

    @Test
    void getByIdInexistenteLanzaNotFound() {
        assertThatThrownBy(() -> service.getById(ReturnId.generate()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listAllDevuelveTodasLasDevoluciones() {
        crearDevolucion();
        crearDevolucion();

        assertThat(service.listAll()).hasSize(2);
    }

    private void prepararOrdenEntregada() {
        orderId = UUID.randomUUID();
        orderItemId = UUID.randomUUID();
        productId = ProductId.generate();
        orders.deliveredAt = Instant.now().minus(Duration.ofDays(1));
        orders.items = List.of(new OrderPort.ItemInfo(orderItemId, productId, 2, usd(900)));
    }

    private Return crearDevolucion() {
        prepararOrdenEntregada();
        return service.create(OrderId.of(orderId),
                new CreateReturnCommand(ReturnReason.DEFECTIVE, List.of(
                        new CreateReturnCommand.ReturnItemCommand(orderItemId, 1))));
    }

    private Return crearDevolucionCompletada() {
        Return aReturn = crearDevolucion();
        aReturn = service.approve(aReturn.getId());
        aReturn = service.registerReturnShipment(aReturn.getId(), "RTN-1");
        aReturn = service.receive(aReturn.getId());
        aReturn = service.inspect(aReturn.getId(), "BUENO", null, true);
        return service.complete(aReturn.getId());
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }

    private static final class FakeReturnRepository implements ReturnRepository {

        private final Map<ReturnId, Return> byId = new HashMap<>();
        private boolean existing;

        @Override
        public Return save(Return aReturn) {
            byId.put(aReturn.getId(), aReturn);
            return aReturn;
        }

        @Override
        public Optional<Return> findById(ReturnId id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Return> findByOrderId(OrderId orderId) {
            return byId.values().stream().filter(r -> r.getOrderId().equals(orderId)).findFirst();
        }

        @Override
        public List<Return> findByCustomerId(UUID customerId) {
            return byId.values().stream().filter(r -> r.getCustomerId().equals(customerId)).toList();
        }

        @Override
        public boolean existsByOrderId(OrderId orderId) {
            return existing || findByOrderId(orderId).isPresent();
        }

        @Override
        public List<Return> findAll() {
            return List.copyOf(byId.values());
        }
    }

    private static final class FakeReturnPolicyRepository implements ReturnPolicyRepository {

        private ReturnPolicy policy = ReturnPolicy.create(ReturnPolicyId.generate(), 30, java.util.Set.of());

        @Override
        public ReturnPolicy save(ReturnPolicy p) {
            this.policy = p;
            return p;
        }

        @Override
        public Optional<ReturnPolicy> findById(ReturnPolicyId id) {
            return Optional.of(policy);
        }

        @Override
        public Optional<ReturnPolicy> findActive() {
            return Optional.of(policy);
        }

        @Override
        public List<ReturnPolicy> findAll() {
            return List.of(policy);
        }
    }

    private static final class FakeOrderPort implements OrderPort {
        private String status = "DELIVERED";
        private Instant deliveredAt = Instant.now().minus(Duration.ofDays(1));
        private List<ItemInfo> items = List.of();

        @Override
        public OrderInfo getOrder(OrderId orderId) {
            return new OrderInfo(UUID.randomUUID(), status, deliveredAt, items);
        }
    }

    private static final class FakePaymentPort implements PaymentPort {
        private boolean refundResult = true;
        private int refundCalls;

        @Override
        public boolean refund(OrderId orderId, Money amount) {
            refundCalls++;
            return refundResult;
        }
    }
}