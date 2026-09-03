package org.fernandodev.ecommerce.checkout.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.cart.application.dto.AddItemCommand;
import org.fernandodev.ecommerce.cart.application.port.in.CartUseCases;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
import org.fernandodev.ecommerce.checkout.application.port.in.CheckoutUseCases;
import org.fernandodev.ecommerce.customer.application.dto.AddressCommand;
import org.fernandodev.ecommerce.customer.application.dto.Profile;
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
import org.fernandodev.ecommerce.inventory.application.port.out.InventoryRepository;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.InventoryId;
import org.fernandodev.ecommerce.payment.adapter.out.MockPaymentGatewayAdapter;
import org.fernandodev.ecommerce.pricing.application.port.out.PriceRepository;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.starter.iam.client.TokenValidator;
import org.fernandodev.starter.iam.dto.ValidarTokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.json.JsonMapper;

@AutoConfigureMockMvc
@Transactional
class CheckoutControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private CustomerUseCases customers;

    @Autowired
    private ProductRepository products;

    @Autowired
    private PriceRepository prices;

    @Autowired
    private InventoryRepository inventories;

    @Autowired
    private CartUseCases carts;

    @Autowired
    private MockPaymentGatewayAdapter paymentGateway;

    @MockitoBean
    private TokenValidator tokenValidator;

    private UUID userId;
    private UUID productId;
    private UUID addressId;

    private void prepararEscenario() {
        userId = UUID.randomUUID();
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(userId, "ana", true, true, Set.of(), Set.of(), Set.of())));

        customers.register(userId, new Profile("Ana", "García", "ana@example.com", null));
        customers.addAddress(userId, new AddressCommand("Calle 1", "Madrid", null, "28001", "España"));
        addressId = customers.getByIamUserId(userId).getAddresses().get(0).id().value();

        productId = UUID.randomUUID();
        products.save(Product.create(ProductId.of(productId),
                new Sku("SKU-" + UUID.randomUUID().toString().substring(0, 8)), null, "Producto", null, null));
        prices.save(Price.define(PriceId.generate(), ProductId.of(productId), null, Money.of("900", "USD")));
        inventories.save(Inventory.define(InventoryId.generate(), ProductId.of(productId), null, 10, 0));
        carts.addItem(userId, new AddItemCommand(ProductId.of(productId), null, 2));
    }

    @Test
    void iniciarCheckoutDevuelve201() throws Exception {
        prepararEscenario();

        mockMvc.perform(post("/api/v1/checkouts")
                        .header("Authorization", "Bearer " + TOKEN)
                        .header("Idempotency-Key", uniqueKey())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"addressId":"%s"}
                                """.formatted(addressId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.subtotal.amount").value(1800.00))
                .andExpect(jsonPath("$.total.amount").value(1800.00));
    }

    @Test
    void startIdempotenteDevuelveElMismoCheckout() throws Exception {
        prepararEscenario();
        String key = uniqueKey();

        String first = iniciarCheckout(key);
        String retry = iniciarCheckout(key);

        assertThat(first).isEqualTo(retry);
    }

    @Test
    void confirmarCheckoutDevuelve200Confirmado() throws Exception {
        prepararEscenario();
        String id = iniciarCheckout(uniqueKey());

        mockMvc.perform(post("/api/v1/checkouts/{id}/confirm", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.paymentId").isNotEmpty());
    }

    @Test
    void confirmarConPagoFallidoDevuelve200Fallido() throws Exception {
        prepararEscenario();
        String id = iniciarCheckout(uniqueKey());
        paymentGateway.setFailAmount(java.math.BigDecimal.valueOf(1800));
        try {
            mockMvc.perform(post("/api/v1/checkouts/{id}/confirm", id)
                            .header("Authorization", "Bearer " + TOKEN))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("FAILED"));
        } finally {
            paymentGateway.setFailAmount(null);
        }
    }

    @Test
    void cancelarCheckoutDevuelve200Cancelado() throws Exception {
        prepararEscenario();
        String id = iniciarCheckout(uniqueKey());

        mockMvc.perform(post("/api/v1/checkouts/{id}/cancel", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void obtenerCheckoutDevuelve200() throws Exception {
        prepararEscenario();
        String id = iniciarCheckout(uniqueKey());

        mockMvc.perform(get("/api/v1/checkouts/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/checkouts/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private String iniciarCheckout(String key) throws Exception {
        String body = mockMvc.perform(post("/api/v1/checkouts")
                        .header("Authorization", "Bearer " + TOKEN)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"addressId":"%s"}
                                """.formatted(addressId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body).get("id").asText();
    }

    private static String uniqueKey() {
        return "checkout-" + UUID.randomUUID();
    }
}