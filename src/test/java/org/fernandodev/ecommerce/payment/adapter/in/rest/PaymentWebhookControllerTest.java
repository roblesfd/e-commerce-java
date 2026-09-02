package org.fernandodev.ecommerce.payment.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.payment.domain.Payment;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
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
class PaymentWebhookControllerTest extends AbstractIntegrationTest {

    private static final String SECRET = "mock-secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private PaymentUseCases paymentUseCases;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of())));
    }

    @Test
    void webhookValidoCapturaElPago() throws Exception {
        autenticarComo();
        Payment payment = paymentUseCases.initiate(OrderId.generate(), Money.of("1000", "USD"),
                "req-" + UUID.randomUUID());
        String transactionId = payment.getProviderTransactionId();

        mockMvc.perform(post("/api/v1/payments/webhooks")
                        .header("X-Webhook-Signature", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"transactionId":"%s","status":"captured"}
                                """.formatted(transactionId)))
                .andExpect(status().isNoContent());

        Payment captured = paymentUseCases.getById(payment.getId());
        assertThat(captured.getStatus()).isEqualTo(org.fernandodev.ecommerce.payment.domain.value_object.PaymentStatus.CAPTURED);
    }

    @Test
    void webhookDuplicadoEsNoOp() throws Exception {
        autenticarComo();
        Payment payment = paymentUseCases.initiate(OrderId.generate(), Money.of("1000", "USD"),
                "req-" + UUID.randomUUID());
        String transactionId = payment.getProviderTransactionId();

        enviarWebhook(transactionId, "captured");
        enviarWebhook(transactionId, "captured");

        Payment captured = paymentUseCases.getById(payment.getId());
        long capturedEvents = captured.getEvents().stream()
                .filter(event -> event.type().equals("CAPTURED"))
                .count();
        assertThat(capturedEvents).isEqualTo(1);
    }

    @Test
    void webhookConFirmaInvalidaDevuelve401() throws Exception {
        autenticarComo();
        Payment payment = paymentUseCases.initiate(OrderId.generate(), Money.of("1000", "USD"),
                "req-" + UUID.randomUUID());

        mockMvc.perform(post("/api/v1/payments/webhooks")
                        .header("X-Webhook-Signature", "secreto-invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"transactionId":"%s","status":"captured"}
                                """.formatted(payment.getProviderTransactionId())))
                .andExpect(status().isUnauthorized());
    }

    private void enviarWebhook(String transactionId, String status) throws Exception {
        mockMvc.perform(post("/api/v1/payments/webhooks")
                        .header("X-Webhook-Signature", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"transactionId":"%s","status":"%s"}
                                """.formatted(transactionId, status)))
                .andExpect(status().isNoContent());
    }
}