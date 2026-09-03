package org.fernandodev.ecommerce.notifications.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.notifications.application.port.in.TemplateUseCases;
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
class TemplateControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private TemplateUseCases templates;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of(), Set.of(), Set.of())));
    }

    @Test
    void crearPlantillaDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/notification-templates")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"ORDER_SHIPPED","title":"Pedido enviado","body":"En camino"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("ORDER_SHIPPED"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void listarPlantillasDevuelveLista() throws Exception {
        autenticarComo();
        templates.create("ORDER_SHIPPED", "Pedido enviado", "En camino");

        mockMvc.perform(get("/api/v1/notification-templates")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void actualizarPlantillaDevuelve200() throws Exception {
        autenticarComo();
        var id = templates.create("ORDER_SHIPPED", "Enviado", "En camino").getId().value();

        mockMvc.perform(put("/api/v1/notification-templates/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Entregado","body":"Entregado correctamente"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Entregado"));
    }

    @Test
    void desactivarYActivarPlantillaDevuelve200() throws Exception {
        autenticarComo();
        var id = templates.create("ORDER_SHIPPED", "Enviado", "En camino").getId().value();

        mockMvc.perform(post("/api/v1/notification-templates/{id}/deactivate", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post("/api/v1/notification-templates/{id}/activate", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.active").value(true));
    }
}