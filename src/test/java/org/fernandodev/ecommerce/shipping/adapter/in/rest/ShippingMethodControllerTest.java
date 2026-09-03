package org.fernandodev.ecommerce.shipping.adapter.in.rest;

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
class ShippingMethodControllerTest extends AbstractIntegrationTest {

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
    void crearMetodoDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/shipping-methods")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Estándar","carrier":"DHL","cost":{"amount":50.00,"currency":"USD"}}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Estándar"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void listarMetodosDevuelveLista() throws Exception {
        autenticarComo();
        crearMetodo();

        mockMvc.perform(get("/api/v1/shipping-methods")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void activarYDesactivarCambianElEstado() throws Exception {
        autenticarComo();
        UUID id = crearMetodo();

        mockMvc.perform(patch("/api/v1/shipping-methods/{id}/deactivate", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/shipping-methods"))
                .andExpect(status().isUnauthorized());
    }

    private UUID crearMetodo() throws Exception {
        String body = mockMvc.perform(post("/api/v1/shipping-methods")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Estándar","carrier":"DHL","cost":{"amount":50.00,"currency":"USD"}}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(jsonMapper.readTree(body).get("id").asText());
    }
}