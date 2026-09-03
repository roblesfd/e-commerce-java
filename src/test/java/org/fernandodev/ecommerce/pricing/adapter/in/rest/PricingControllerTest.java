package org.fernandodev.ecommerce.pricing.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.starter.iam.client.TokenValidator;
import org.fernandodev.starter.iam.dto.ValidarTokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@AutoConfigureMockMvc
@Transactional
class PricingControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of(), Set.of(), Set.of())));
    }

    @Test
    void definirPrecioBaseDevuelve201() throws Exception {
        autenticarComo();
        UUID productId = UUID.randomUUID();

        mockMvc.perform(post("/api/v1/products/{productId}/prices", productId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":900.00,"currency":"USD","variantId":null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productId").value(productId.toString()))
                .andExpect(jsonPath("$.basePrice.amount").value(900.00))
                .andExpect(jsonPath("$.basePrice.currency").value("USD"));
    }

    @Test
    void definirPrecioDuplicadoDevuelve409() throws Exception {
        autenticarComo();
        UUID productId = UUID.randomUUID();
        definirPrecio(productId);

        mockMvc.perform(post("/api/v1/products/{productId}/prices", productId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":800.00,"currency":"USD","variantId":null}
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void definirPrecioConImporteNoPositivoDevuelve400() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/products/{productId}/prices", UUID.randomUUID())
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":-1,"currency":"USD","variantId":null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerPrecioActualDevuelve200() throws Exception {
        autenticarComo();
        UUID productId = UUID.randomUUID();
        definirPrecio(productId);

        mockMvc.perform(get("/api/v1/products/{productId}/prices/current", productId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(900.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void obtenerPrecioActualSinDefinirDevuelve404() throws Exception {
        autenticarComo();

        mockMvc.perform(get("/api/v1/products/{productId}/prices/current", UUID.randomUUID())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerPrecioPorIdDevuelve200() throws Exception {
        autenticarComo();
        UUID priceId = definirPrecio(UUID.randomUUID());

        mockMvc.perform(get("/api/v1/prices/{priceId}", priceId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basePrice.amount").value(900.00));
    }

    @Test
    void actualizarPrecioBaseDevuelve200() throws Exception {
        autenticarComo();
        UUID priceId = definirPrecio(UUID.randomUUID());

        mockMvc.perform(put("/api/v1/prices/{priceId}", priceId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":1000.00,"currency":"USD"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basePrice.amount").value(1000.00))
                .andExpect(jsonPath("$.history[0].amount").value(900.00));
    }

    @Test
    void anadirOverridePromocionalDevuelve201() throws Exception {
        autenticarComo();
        UUID priceId = definirPrecio(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/prices/{priceId}/overrides", priceId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":700.00,"currency":"USD",
                                 "validFrom":"2026-01-01T00:00:00Z","validTo":"2026-02-01T00:00:00Z",
                                 "type":"PROMOTIONAL"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.overrides[0].amount").value(700.00))
                .andExpect(jsonPath("$.overrides[0].type").value("PROMOTIONAL"));
    }

    @Test
    void anadirOverridePromocionalMayorQueBaseDevuelve422() throws Exception {
        autenticarComo();
        UUID priceId = definirPrecio(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/prices/{priceId}/overrides", priceId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":1000.00,"currency":"USD",
                                 "validFrom":"2026-01-01T00:00:00Z","validTo":"2026-02-01T00:00:00Z",
                                 "type":"PROMOTIONAL"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void eliminarOverrideDevuelve204() throws Exception {
        autenticarComo();
        UUID priceId = definirPrecio(UUID.randomUUID());
        UUID overrideId = anadirOverride(priceId);

        mockMvc.perform(delete("/api/v1/prices/{priceId}/overrides/{overrideId}", priceId, overrideId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void snapshotDevuelveBaseVigenteYDescuento() throws Exception {
        autenticarComo();
        UUID productId = UUID.randomUUID();
        UUID priceId = definirPrecio(productId);
        anadirOverride(priceId);

        mockMvc.perform(get("/api/v1/products/{productId}/prices/snapshot", productId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.basePrice.amount").value(900.00))
                .andExpect(jsonPath("$.amount.amount").value(700.00))
                .andExpect(jsonPath("$.discount.amount").value(200.00));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/prices/{priceId}", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private UUID definirPrecio(UUID productId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/products/{productId}/prices", productId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":900.00,"currency":"USD","variantId":null}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(jsonMapper.readTree(body).get("id").asText());
    }

    private UUID anadirOverride(UUID priceId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/prices/{priceId}/overrides", priceId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"amount":700.00,"currency":"USD",
                                 "validFrom":"2026-01-01T00:00:00Z","validTo":"2030-02-01T00:00:00Z",
                                 "type":"PROMOTIONAL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode overrides = jsonMapper.readTree(body).get("overrides");
        return UUID.fromString(overrides.get(0).get("id").asText());
    }
}