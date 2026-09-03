package org.fernandodev.ecommerce.returns.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.customer.application.dto.AddressCommand;
import org.fernandodev.ecommerce.customer.application.dto.Profile;
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
import org.fernandodev.ecommerce.order.application.dto.CreateOrderCommand;
import org.fernandodev.ecommerce.order.application.dto.OrderItemCommand;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.returns.application.dto.CreateReturnPolicyCommand;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnPolicyUseCases;
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
class ReturnControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private CustomerUseCases customers;

    @Autowired
    private OrderUseCases orders;

    @Autowired
    private PaymentUseCases payments;

    @Autowired
    private ReturnPolicyUseCases policies;

    @MockitoBean
    private TokenValidator tokenValidator;

    private UUID userId;
    private UUID addressId;

    private void prepararCliente() {
        userId = UUID.randomUUID();
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(userId, "ana", true, true, Set.of(), Set.of(), Set.of())));
        customers.register(userId, new Profile("Ana", "García", "ana@example.com", null));
        customers.addAddress(userId, new AddressCommand("Calle 1", "Madrid", null, "28001", "España"));
        addressId = customers.getByIamUserId(userId).getAddresses().get(0).id().value();
    }

    private void prepararPolitica() {
        policies.create(new CreateReturnPolicyCommand(30, Set.of()));
    }

    private UUID crearOrdenEntregada() {
        UUID orderId = crearOrden();
        OrderId id = OrderId.of(orderId);
        orders.confirm(id);
        orders.process(id);
        orders.markReadyToShip(id);
        orders.markShipped(id);
        orders.markDelivered(id);
        capturarPago(orderId);
        return orderId;
    }

    private UUID crearOrden() {
        return orders.create(new CreateOrderCommand(userId, addressId, "Calle 1, Madrid, España", null,
                List.of(new OrderItemCommand(ProductId.generate(), null, "CAMISA", "Camisa", 1, usd(900))),
                usd(900), usd(0), usd(900))).getId().value();
    }

    private void capturarPago(UUID orderId) {
        var payment = payments.initiate(OrderId.of(orderId), usd(900), UUID.randomUUID().toString());
        payments.capture(payment.getId());
    }

    private UUID orderItemId(UUID orderId) {
        return orders.getById(OrderId.of(orderId)).getItems().get(0).id().value();
    }

    @Test
    void crearDevolucionParaOrdenEntregadaDevuelve201() throws Exception {
        prepararCliente();
        prepararPolitica();
        UUID orderId = crearOrdenEntregada();

        mockMvc.perform(post("/api/v1/returns")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","reason":"DEFECTIVE","items":[{"orderItemId":"%s","quantity":1}]}
                                """.formatted(orderId, orderItemId(orderId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("REQUESTED"))
                .andExpect(jsonPath("$.items[0].quantity").value(1));
    }

    @Test
    void crearDevolucionParaOrdenNoEntregadaDevuelve422() throws Exception {
        prepararCliente();
        prepararPolitica();
        UUID orderId = crearOrden();

        mockMvc.perform(post("/api/v1/returns")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","reason":"DEFECTIVE","items":[{"orderItemId":"%s","quantity":1}]}
                                """.formatted(orderId, orderItemId(orderId))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cicloHastaCompletadoConRefundDevuelve200() throws Exception {
        prepararCliente();
        prepararPolitica();
        UUID orderId = crearOrdenEntregada();
        String id = crearDevolucion(orderId);

        mockMvc.perform(post("/api/v1/returns/{id}/approve", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.refundAmount").value(900.00));
        mockMvc.perform(post("/api/v1/returns/{id}/return-shipment", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"trackingNumber":"RTN-1"}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("IN_TRANSIT"));
        mockMvc.perform(post("/api/v1/returns/{id}/receive", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RECEIVED"));
        mockMvc.perform(post("/api/v1/returns/{id}/inspect", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"condition":"BUENO","notes":"sin daños","passed":true}
                                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("INSPECTED"));
        mockMvc.perform(post("/api/v1/returns/{id}/complete", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.refundStatus").value("REFUNDED"));
    }

    @Test
    void listarMisDevolucionesDevuelveLista() throws Exception {
        prepararCliente();
        prepararPolitica();
        UUID orderId = crearOrdenEntregada();
        crearDevolucion(orderId);

        mockMvc.perform(get("/api/v1/returns/me")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void obtenerDevolucionPorOrdenDevuelve200() throws Exception {
        prepararCliente();
        prepararPolitica();
        UUID orderId = crearOrdenEntregada();
        crearDevolucion(orderId);

        mockMvc.perform(get("/api/v1/returns").param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/returns/me"))
                .andExpect(status().isUnauthorized());
    }

    private String crearDevolucion(UUID orderId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/returns")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","reason":"DEFECTIVE","items":[{"orderItemId":"%s","quantity":1}]}
                                """.formatted(orderId, orderItemId(orderId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body).get("id").asText();
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}