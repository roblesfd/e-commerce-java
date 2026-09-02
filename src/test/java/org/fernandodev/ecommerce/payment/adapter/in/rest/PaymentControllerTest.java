package org.fernandodev.ecommerce.payment.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.payment.application.port.out.PaymentRepository;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.payment.domain.value_object.PaymentId;
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
class PaymentControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private PaymentRepository payments;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of())));
    }

    @Test
    void iniciarPagoDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + TOKEN)
                        .header("Idempotency-Key", "req-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","amount":1000.00,"currency":"USD"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.amount.amount").value(1000.00));
    }

    @Test
    void capturarDevuelve200() throws Exception {
        autenticarComo();
        String id = iniciarPago();

        mockMvc.perform(post("/api/v1/payments/{id}/capture", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CAPTURED"));
    }

    @Test
    void reembolsarDevuelve200() throws Exception {
        autenticarComo();
        String id = iniciarPago();
        capturar(id);

        mockMvc.perform(post("/api/v1/payments/{id}/refund", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"));
    }

    @Test
    void cancelarDevuelve200() throws Exception {
        autenticarComo();
        String id = iniciarPago();

        mockMvc.perform(post("/api/v1/payments/{id}/cancel", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void rechazarPagoPendingDevuelve200() throws Exception {
        autenticarComo();
        UUID id = payments.save(Payment.create(PaymentId.generate(), OrderId.generate(),
                "req-" + UUID.randomUUID(), Money.of("1000", "USD"), Instant.now())).getId().value();

        mockMvc.perform(post("/api/v1/payments/{id}/reject", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void obtenerDevuelve200() throws Exception {
        autenticarComo();
        String id = iniciarPago();

        mockMvc.perform(get("/api/v1/payments/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AUTHORIZED"));
    }

    @Test
    void obtenerPorOrdenDevuelve200() throws Exception {
        autenticarComo();
        UUID orderId = UUID.randomUUID();
        iniciarPago(orderId);

        mockMvc.perform(get("/api/v1/payments").param("orderId", orderId.toString())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/payments/{id}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private String iniciarPago() throws Exception {
        return iniciarPago(UUID.randomUUID());
    }

    private String iniciarPago(UUID orderId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/payments")
                        .header("Authorization", "Bearer " + TOKEN)
                        .header("Idempotency-Key", "req-" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"orderId":"%s","amount":1000.00,"currency":"USD"}
                                """.formatted(orderId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body).get("id").asText();
    }

    private void capturar(String id) throws Exception {
        mockMvc.perform(post("/api/v1/payments/{id}/capture", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk());
    }
}