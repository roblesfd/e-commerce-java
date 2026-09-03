package org.fernandodev.ecommerce.shipping.adapter.in.rest;

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
import org.fernandodev.ecommerce.shipping.application.port.in.ShippingMethodUseCases;
import org.fernandodev.ecommerce.shipping.application.dto.CreateShippingMethodCommand;
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
class ShipmentControllerTest extends AbstractIntegrationTest {

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
    private ShippingMethodUseCases methods;

    @MockitoBean
    private TokenValidator tokenValidator;

    private UUID userId;
    private UUID addressId;
    private UUID methodId;

    private void prepararEscenario() {
        userId = UUID.randomUUID();
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(userId, "ana", true, true, Set.of(), Set.of(), Set.of())));
        customers.register(userId, new Profile("Ana", "García", "ana@example.com", null));
        customers.addAddress(userId, new AddressCommand("Calle 1", "Madrid", null, "28001", "España"));
        addressId = customers.getByIamUserId(userId).getAddresses().get(0).id().value();
        methodId = methods.create(new CreateShippingMethodCommand("Estándar", "DHL", usd(50))).getId().value();
    }

    @Test
    void crearEnvioParaOrdenConfirmadaDevuelve201() throws Exception {
        prepararEscenario();
        UUID orderId = crearOrdenConfirmada();

        mockMvc.perform(post("/api/v1/shipments")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","shippingMethodId":"%s","addressId":"%s"}
                                """.formatted(orderId, methodId, addressId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.trackingNumber").isNotEmpty())
                .andExpect(jsonPath("$.cost.amount").value(50.00));
    }

    @Test
    void crearEnvioParaOrdenNoConfirmadaDevuelve422() throws Exception {
        prepararEscenario();
        UUID orderId = orders.create(comandoOrden()).getId().value();

        mockMvc.perform(post("/api/v1/shipments")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","shippingMethodId":"%s","addressId":"%s"}
                                """.formatted(orderId, methodId, addressId)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cicloHastaEntregadoDevuelve200() throws Exception {
        prepararEscenario();
        UUID orderId = crearOrdenConfirmada();
        String id = crearEnvio(orderId);
        orders.process(org.fernandodev.ecommerce.order.domain.value_object.OrderId.of(orderId));
        orders.markReadyToShip(org.fernandodev.ecommerce.order.domain.value_object.OrderId.of(orderId));

        mockMvc.perform(post("/api/v1/shipments/{id}/prepared", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PREPARING"));
        mockMvc.perform(post("/api/v1/shipments/{id}/shipped", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SHIPPED"));
        mockMvc.perform(post("/api/v1/shipments/{id}/in-transit", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("IN_TRANSIT"));
        mockMvc.perform(post("/api/v1/shipments/{id}/out-for-delivery", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("OUT_FOR_DELIVERY"));
        mockMvc.perform(post("/api/v1/shipments/{id}/delivered", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void listarMisEnviosDevuelveLista() throws Exception {
        prepararEscenario();
        UUID orderId = crearOrdenConfirmada();
        crearEnvio(orderId);

        mockMvc.perform(get("/api/v1/shipments/me")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void obtenerEnvioPorOrdenDevuelve200() throws Exception {
        prepararEscenario();
        UUID orderId = crearOrdenConfirmada();
        crearEnvio(orderId);

        mockMvc.perform(get("/api/v1/shipments").param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/shipments/me"))
                .andExpect(status().isUnauthorized());
    }

    private UUID crearOrdenConfirmada() {
        UUID orderId = orders.create(comandoOrden()).getId().value();
        orders.confirm(org.fernandodev.ecommerce.order.domain.value_object.OrderId.of(orderId));
        return orderId;
    }

    private CreateOrderCommand comandoOrden() {
        return new CreateOrderCommand(userId, addressId, "Calle 1, Madrid, España", null,
                List.of(new OrderItemCommand(ProductId.generate(), null, "CAMISA", "Camisa", 1, usd(900))),
                usd(900), usd(0), usd(900));
    }

    private String crearEnvio(UUID orderId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/shipments")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","shippingMethodId":"%s","addressId":"%s"}
                                """.formatted(orderId, methodId, addressId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body).get("id").asText();
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}