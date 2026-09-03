package org.fernandodev.ecommerce.admin.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminSystemUseCases;
import org.fernandodev.ecommerce.admin.application.port.out.AuditRepository;
import org.fernandodev.ecommerce.admin.application.port.out.CurrentActorPort;
import org.fernandodev.ecommerce.admin.application.port.out.SystemConfigRepository;
import org.fernandodev.ecommerce.admin.domain.AuditEntry;
import org.fernandodev.ecommerce.admin.domain.SystemConfig;
import org.fernandodev.ecommerce.admin.domain.value_object.AuditEntryId;
import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.dto.PageResult;
import org.fernandodev.ecommerce.catalog.application.dto.ProductSearch;
import org.fernandodev.ecommerce.catalog.application.port.in.BrandUseCases;
import org.fernandodev.ecommerce.catalog.application.port.in.CategoryUseCases;
import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
import org.fernandodev.ecommerce.customer.domain.Customer;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.domain.Order;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnUseCases;
import org.fernandodev.ecommerce.returns.domain.Return;
import org.fernandodev.ecommerce.reviews.application.port.in.ReviewUseCases;
import org.fernandodev.ecommerce.reviews.domain.Review;
import org.fernandodev.ecommerce.search.application.port.in.IndexUseCases;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import org.junit.jupiter.api.Test;

class AdminSystemServiceTest {

    private final FakeSystemConfigRepository configs = new FakeSystemConfigRepository();
    private final AuditServiceTest.FakeAuditRepository audit = new AuditServiceTest.FakeAuditRepository();
    private final CurrentActorPort actor = () -> UUID.randomUUID();
    private final AuditService auditService = new AuditService(audit, actor);
    private final FakeCustomerUseCases customers = new FakeCustomerUseCases();
    private final FakeProductUseCases products = new FakeProductUseCases();
    private final FakeCategoryUseCases categories = new FakeCategoryUseCases();
    private final FakeBrandUseCases brands = new FakeBrandUseCases();
    private final FakeOrderUseCases orders = new FakeOrderUseCases();
    private final FakePaymentUseCases payments = new FakePaymentUseCases();
    private final FakeReturnUseCases returns = new FakeReturnUseCases();
    private final FakeReviewUseCases reviews = new FakeReviewUseCases();
    private final FakeIndexUseCases index = new FakeIndexUseCases();
    private final AdminSystemService service = new AdminSystemService(
            configs, audit, auditService, customers, products, categories, brands,
            orders, payments, returns, reviews, index);

    @Test
    void setConfigCreaYActualizaLaClave() {
        service.setConfig("order.returnWindowDays", "30");
        service.setConfig("order.returnWindowDays", "15");

        Optional<SystemConfig> config = service.getConfig("order.returnWindowDays");
        assertThat(config).isPresent();
        assertThat(config.orElseThrow().getValue()).isEqualTo("15");
        assertThat(audit.findByAction("SET_CONFIG")).hasSize(2);
    }

    @Test
    void listAuditFiltraPorAccion() {
        audit.save(AuditEntry.create(AuditEntryId.generate(), UUID.randomUUID(), "DELETE_PRODUCT",
                "product", UUID.randomUUID(), "d", java.time.Instant.now()));
        audit.save(AuditEntry.create(AuditEntryId.generate(), UUID.randomUUID(), "REFUND_PAYMENT",
                "payment", UUID.randomUUID(), "d", java.time.Instant.now()));

        assertThat(service.listAudit("DELETE_PRODUCT", null)).hasSize(1);
        assertThat(service.listAudit(null, null)).hasSize(2);
    }

    @Test
    void metricsAgregaLosConteosDeLosContextos() {
        customers.count = 2;
        products.count = 3;
        categories.count = 1;
        brands.count = 1;
        orders.count = 4;
        payments.count = 5;
        returns.count = 1;
        reviews.pending = 2;

        AdminSystemUseCases.Metrics metrics = service.metrics();

        assertThat(metrics.customers()).isEqualTo(2);
        assertThat(metrics.products()).isEqualTo(3);
        assertThat(metrics.orders()).isEqualTo(4);
        assertThat(metrics.pendingReviews()).isEqualTo(2);
    }

    @Test
    void integrationFailuresCuentaLosDocumentosFallidosDelIndice() {
        index.failures = 3;

        assertThat(service.integrationFailures()).isEqualTo(3);
    }

    private static final class FakeSystemConfigRepository implements SystemConfigRepository {
        private final Map<String, SystemConfig> byKey = new HashMap<>();

        @Override
        public SystemConfig save(SystemConfig config) {
            byKey.put(config.getKey(), config);
            return config;
        }

        @Override
        public Optional<SystemConfig> findById(String key) {
            return Optional.ofNullable(byKey.get(key));
        }

        @Override
        public List<SystemConfig> findAll() {
            return List.copyOf(byKey.values());
        }
    }

    private static final class FakeCustomerUseCases implements CustomerUseCases {
        private int count;

        @Override
        public List<Customer> listAll() {
            return java.util.Collections.nCopies(count, (Customer) null);
        }

        @Override
        public org.fernandodev.ecommerce.customer.domain.Customer register(UUID iamUserId,
                org.fernandodev.ecommerce.customer.application.dto.Profile profile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.fernandodev.ecommerce.customer.domain.Customer getByIamUserId(UUID iamUserId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.fernandodev.ecommerce.customer.domain.Customer updateProfile(UUID iamUserId,
                org.fernandodev.ecommerce.customer.application.dto.Profile profile) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.fernandodev.ecommerce.customer.domain.Customer addAddress(UUID iamUserId,
                org.fernandodev.ecommerce.customer.application.dto.AddressCommand address) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.fernandodev.ecommerce.customer.domain.Customer updateAddress(UUID iamUserId, java.util.UUID addressId,
                org.fernandodev.ecommerce.customer.application.dto.AddressCommand address) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.fernandodev.ecommerce.customer.domain.Customer removeAddress(UUID iamUserId, java.util.UUID addressId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.fernandodev.ecommerce.customer.domain.Customer setDefaultAddress(UUID iamUserId,
                java.util.UUID addressId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.fernandodev.ecommerce.customer.domain.Customer updatePreferences(UUID iamUserId,
                org.fernandodev.ecommerce.customer.application.dto.PreferencesCommand preferences) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<org.fernandodev.ecommerce.customer.domain.value_object.Address> listAddresses(UUID iamUserId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeProductUseCases implements ProductUseCases {
        private int count;

        @Override
        public PageResult<Product> list(ProductSearch search, PageQuery query) {
            return PageResult.of(java.util.Collections.nCopies(count, (Product) null), 0, 1, count);
        }

        @Override
        public Product create(org.fernandodev.ecommerce.catalog.application.dto.ProductCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Product getById(ProductId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Product update(ProductId id, org.fernandodev.ecommerce.catalog.application.dto.ProductCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Product activate(ProductId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Product deactivate(ProductId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(ProductId id) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeCategoryUseCases implements CategoryUseCases {
        private int count;

        @Override
        public List<Category> list() {
            return java.util.Collections.nCopies(count, (Category) null);
        }

        @Override
        public Category create(org.fernandodev.ecommerce.catalog.application.dto.CategoryCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Category getById(CategoryId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Category update(CategoryId id, org.fernandodev.ecommerce.catalog.application.dto.CategoryCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Category activate(CategoryId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Category deactivate(CategoryId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(CategoryId id) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeBrandUseCases implements BrandUseCases {
        private int count;

        @Override
        public List<Brand> list() {
            return java.util.Collections.nCopies(count, (Brand) null);
        }

        @Override
        public Brand create(org.fernandodev.ecommerce.catalog.application.dto.BrandCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Brand getById(BrandId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Brand update(BrandId id, org.fernandodev.ecommerce.catalog.application.dto.BrandCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Brand activate(BrandId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Brand deactivate(BrandId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(BrandId id) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeOrderUseCases implements OrderUseCases {
        private int count;

        @Override
        public List<Order> listAll() {
            return java.util.Collections.nCopies(count, (Order) null);
        }

        @Override
        public Order create(org.fernandodev.ecommerce.order.application.dto.CreateOrderCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Order getById(org.fernandodev.ecommerce.order.domain.value_object.OrderId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Order> listByCustomer(UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Order confirm(org.fernandodev.ecommerce.order.domain.value_object.OrderId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Order process(org.fernandodev.ecommerce.order.domain.value_object.OrderId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Order markReadyToShip(org.fernandodev.ecommerce.order.domain.value_object.OrderId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Order markShipped(org.fernandodev.ecommerce.order.domain.value_object.OrderId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Order markDelivered(org.fernandodev.ecommerce.order.domain.value_object.OrderId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Order cancel(org.fernandodev.ecommerce.order.domain.value_object.OrderId id) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakePaymentUseCases implements PaymentUseCases {
        private int count;

        @Override
        public List<Payment> listAll() {
            return java.util.Collections.nCopies(count, (Payment) null);
        }

        @Override
        public Payment initiate(org.fernandodev.ecommerce.order.domain.value_object.OrderId orderId,
                org.fernandodev.ecommerce.shared.domain.Money amount, String requestId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Payment authorize(org.fernandodev.ecommerce.payment.domain.value_object.PaymentId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Payment capture(org.fernandodev.ecommerce.payment.domain.value_object.PaymentId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Payment reject(org.fernandodev.ecommerce.payment.domain.value_object.PaymentId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Payment cancel(org.fernandodev.ecommerce.payment.domain.value_object.PaymentId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Payment refund(org.fernandodev.ecommerce.payment.domain.value_object.PaymentId id,
                org.fernandodev.ecommerce.shared.domain.Money amount) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Payment getById(org.fernandodev.ecommerce.payment.domain.value_object.PaymentId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Payment> getByOrder(org.fernandodev.ecommerce.order.domain.value_object.OrderId orderId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Payment processWebhook(String transactionId, String providerStatus) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeReturnUseCases implements ReturnUseCases {
        private int count;

        @Override
        public List<Return> listAll() {
            return java.util.Collections.nCopies(count, (Return) null);
        }

        @Override
        public Return create(org.fernandodev.ecommerce.order.domain.value_object.OrderId orderId,
                org.fernandodev.ecommerce.returns.application.dto.CreateReturnCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Return getById(org.fernandodev.ecommerce.returns.domain.value_object.ReturnId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Return> listByCustomer(UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Return> getByOrder(org.fernandodev.ecommerce.order.domain.value_object.OrderId orderId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Return approve(org.fernandodev.ecommerce.returns.domain.value_object.ReturnId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Return reject(org.fernandodev.ecommerce.returns.domain.value_object.ReturnId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Return cancel(org.fernandodev.ecommerce.returns.domain.value_object.ReturnId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Return registerReturnShipment(org.fernandodev.ecommerce.returns.domain.value_object.ReturnId id,
                String trackingNumber) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Return receive(org.fernandodev.ecommerce.returns.domain.value_object.ReturnId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Return inspect(org.fernandodev.ecommerce.returns.domain.value_object.ReturnId id, String condition,
                String notes, boolean passed) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Return complete(org.fernandodev.ecommerce.returns.domain.value_object.ReturnId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Return retryRefund(org.fernandodev.ecommerce.returns.domain.value_object.ReturnId id) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeReviewUseCases implements ReviewUseCases {
        private int pending;

        @Override
        public List<Review> listForModeration() {
            return java.util.Collections.nCopies(pending, (Review) null);
        }

        @Override
        public Review create(UUID customerId, org.fernandodev.ecommerce.catalog.domain.value_object.ProductId productId,
                org.fernandodev.ecommerce.reviews.application.dto.CreateReviewCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Review update(org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId id,
                org.fernandodev.ecommerce.reviews.application.dto.UpdateReviewCommand command) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Review delete(org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Review approve(org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Review reject(org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Review hide(org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Review restore(org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Review addReply(org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId id, UUID authorId,
                String comment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Review getById(org.fernandodev.ecommerce.reviews.domain.value_object.ReviewId id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Review> listByProduct(org.fernandodev.ecommerce.catalog.domain.value_object.ProductId productId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Review> listByCustomer(UUID customerId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public org.fernandodev.ecommerce.reviews.application.dto.RatingSummary ratingSummary(
                org.fernandodev.ecommerce.catalog.domain.value_object.ProductId productId) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeIndexUseCases implements IndexUseCases {
        private int failures;

        @Override
        public List<SearchDocument> listIndexFailures() {
            return java.util.Collections.nCopies(failures, (SearchDocument) null);
        }

        @Override
        public SearchDocument indexProduct(ProductId productId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeProduct(ProductId productId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void rebuild() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SearchDocument retryIndexing(ProductId productId) {
            throw new UnsupportedOperationException();
        }
    }
}