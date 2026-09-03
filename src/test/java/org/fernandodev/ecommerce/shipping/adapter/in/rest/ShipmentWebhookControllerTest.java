package org.fernandodev.ecommerce.shipping.adapter.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shipping.application.port.out.ShipmentRepository;
import org.fernandodev.ecommerce.shipping.application.port.out.ShippingMethodRepository;
import org.fernandodev.ecommerce.shipping.domain.Shipment;
import org.fernandodev.ecommerce.shipping.domain.ShippingMethod;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentId;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShipmentStatus;
import org.fernandodev.ecommerce.shipping.domain.value_object.ShippingMethodId;
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

@AutoConfigureMockMvc
@Transactional
class ShipmentWebhookControllerTest extends AbstractIntegrationTest {

    private static final String SECRET = "mock-secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShipmentRepository repository;

    @Autowired
    private ShippingMethodRepository methods;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of(), Set.of(), Set.of())));
    }

    @Test
    void webhookValidoAvanzaElEnvio() throws Exception {
        autenticarComo();
        Shipment shipment = guardarEnvio("DHL-TRACK1");

        mockMvc.perform(post("/api/v1/shipments/webhooks")
                        .header("X-Webhook-Signature", SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"trackingNumber":"DHL-TRACK1","status":"prepared"}
                                """))
                .andExpect(status().isNoContent());

        Shipment loaded = repository.findById(shipment.getId()).orElseThrow();
        assertThat(loaded.getStatus()).isEqualTo(ShipmentStatus.PREPARING);
    }

    @Test
    void webhookConFirmaInvalidaDevuelve401() throws Exception {
        autenticarComo();
        guardarEnvio("DHL-TRACK2");

        mockMvc.perform(post("/api/v1/shipments/webhooks")
                        .header("X-Webhook-Signature", "secreto-invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"trackingNumber":"DHL-TRACK2","status":"prepared"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private Shipment guardarEnvio(String tracking) {
        ShippingMethodId methodId = methods.save(ShippingMethod.create(
                ShippingMethodId.generate(), "Estándar", "DHL", Money.of("50", "USD"))).getId();
        return repository.save(Shipment.create(ShipmentId.generate(), OrderId.generate(), UUID.randomUUID(),
                methodId, UUID.randomUUID(), "Calle 1, Madrid, España",
                tracking, Money.of("50", "USD"), Instant.now()));
    }
}