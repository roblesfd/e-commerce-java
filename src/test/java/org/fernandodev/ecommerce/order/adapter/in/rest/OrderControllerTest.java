package org.fernandodev.ecommerce.order.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.order.application.dto.CreateOrderCommand;
import org.fernandodev.ecommerce.order.application.dto.OrderItemCommand;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.fernandodev.starter.iam.client.TokenValidator;
import org.fernandodev.starter.iam.dto.ValidarTokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
class OrderControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderUseCases orderUseCases;

    @MockitoBean
    private TokenValidator tokenValidator;

    private UUID userId;

    private void autenticarComo() {
        userId = UUID.randomUUID();
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(userId, "ana", true, true, Set.of())));
    }

    @Test
    void obtenerOrdenPorIdDevuelve200() throws Exception {
        autenticarComo();
        UUID id = crearOrden();

        mockMvc.perform(get("/api/v1/orders/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.items[0].sku").value("CAMISA"));
    }

    @Test
    void obtenerOrdenInexistenteDevuelve404() throws Exception {
        autenticarComo();

        mockMvc.perform(get("/api/v1/orders/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarMisOrdenesDevuelveLista() throws Exception {
        autenticarComo();
        crearOrden();

        mockMvc.perform(get("/api/v1/orders/me")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void confirmarOrdenDevuelve200() throws Exception {
        autenticarComo();
        UUID id = crearOrden();

        mockMvc.perform(patch("/api/v1/orders/{id}/confirm", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void cancelarOrdenDevuelve200() throws Exception {
        autenticarComo();
        UUID id = crearOrden();

        mockMvc.perform(patch("/api/v1/orders/{id}/cancel", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/orders/me"))
                .andExpect(status().isUnauthorized());
    }

    private UUID crearOrden() {
        return orderUseCases.create(new CreateOrderCommand(
                userId, UUID.randomUUID(), "Calle 1, Madrid, España", "WELCOME10",
                List.of(new OrderItemCommand(ProductId.generate(), null, "CAMISA", "Camisa", 2, usd(900))),
                usd(1800), usd(180), usd(1620))).getId().value();
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}