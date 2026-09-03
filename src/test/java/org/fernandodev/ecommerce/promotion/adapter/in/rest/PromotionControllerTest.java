package org.fernandodev.ecommerce.promotion.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

import tools.jackson.databind.json.JsonMapper;

@AutoConfigureMockMvc
@Transactional
class PromotionControllerTest extends AbstractIntegrationTest {

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
    void crearPromocionDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/promotions")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"10% off","type":"PERCENTAGE","percentageValue":10.00,
                                 "validFrom":"2026-01-01T00:00:00Z","validTo":"2099-12-31T00:00:00Z",
                                 "maxUsage":0,"maxUsagePerCustomer":0,"productIds":[],"categoryIds":[]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("10% off"))
                .andExpect(jsonPath("$.type").value("PERCENTAGE"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void obtenerPromocionDevuelve200() throws Exception {
        autenticarComo();
        UUID id = crearPromocion();

        mockMvc.perform(get("/api/v1/promotions/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("10% off"));
    }

    @Test
    void activarYDesactivarCambianElEstado() throws Exception {
        autenticarComo();
        UUID id = crearPromocion();

        mockMvc.perform(patch("/api/v1/promotions/{id}/deactivate", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch("/api/v1/promotions/{id}/activate", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void registrarUsoDevuelve200YIncrementaElContador() throws Exception {
        autenticarComo();
        UUID id = crearPromocion();

        mockMvc.perform(post("/api/v1/promotions/{id}/usage", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.usedCount").value(1));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/promotions"))
                .andExpect(status().isUnauthorized());
    }

    private UUID crearPromocion() throws Exception {
        String body = mockMvc.perform(post("/api/v1/promotions")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"10% off","type":"PERCENTAGE","percentageValue":10.00,
                                 "validFrom":"2026-01-01T00:00:00Z","validTo":"2099-12-31T00:00:00Z",
                                 "maxUsage":0,"maxUsagePerCustomer":0,"productIds":[],"categoryIds":[]}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(jsonMapper.readTree(body).get("id").asText());
    }
}