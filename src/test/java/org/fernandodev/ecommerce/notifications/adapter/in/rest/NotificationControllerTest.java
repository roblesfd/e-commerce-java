package org.fernandodev.ecommerce.notifications.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.notifications.application.port.out.NotificationProviderPort;
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
class NotificationControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private TokenValidator tokenValidator;

    @MockitoBean
    private NotificationProviderPort notificationProvider;

    private UUID userId;

    private void autenticarComo() {
        userId = UUID.randomUUID();
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(userId, "ana", true, true, Set.of(), Set.of(), Set.of())));
        when(notificationProvider.send(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(NotificationProviderPort.DeliveryResult.ok());
    }

    @Test
    void crearNotificacionInAppDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"%s","channel":"IN_APP","title":"Pedido enviado","body":"En camino"}
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SENT"))
                .andExpect(jsonPath("$.read").value(false));
    }

    @Test
    void crearNotificacionEmailDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"%s","channel":"EMAIL","title":"Pedido enviado","body":"En camino"}
                                """.formatted(userId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void listarMisNotificacionesDevuelveLista() throws Exception {
        autenticarComo();
        crearNotificacion();

        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listarNoLeidasDevuelveFiltro() throws Exception {
        autenticarComo();
        String a = crearNotificacion();
        crearNotificacion();
        mockMvc.perform(post("/api/v1/notifications/{id}/read", a)
                .header("Authorization", "Bearer " + TOKEN)).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/notifications").param("unread", "true")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void marcarLeidaYNoLeidaDevuelve200() throws Exception {
        autenticarComo();
        String id = crearNotificacion();

        mockMvc.perform(post("/api/v1/notifications/{id}/read", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.read").value(true));

        mockMvc.perform(post("/api/v1/notifications/{id}/unread", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.read").value(false));
    }

    @Test
    void marcarTodasComoLeidasDevuelve204() throws Exception {
        autenticarComo();
        crearNotificacion();
        crearNotificacion();

        mockMvc.perform(post("/api/v1/notifications/read-all")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/notifications").param("unread", "true")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void eliminarNotificacionDevuelve204() throws Exception {
        autenticarComo();
        String id = crearNotificacion();

        mockMvc.perform(delete("/api/v1/notifications/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void reintentarFallidaDevuelve200() throws Exception {
        autenticarComo();
        when(notificationProvider.send(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(NotificationProviderPort.DeliveryResult.fail());
        String id = crearNotificacionConCanal("PUSH");
        mockMvc.perform(get("/api/v1/notifications/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(jsonPath("$.status").value("FAILED"));

        when(notificationProvider.send(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(NotificationProviderPort.DeliveryResult.ok());

        mockMvc.perform(post("/api/v1/notifications/{id}/retry", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SENT"));
    }

    @Test
    void crearConDedupDuplicadoDevuelve409() throws Exception {
        autenticarComo();
        String body = """
                {"customerId":"%s","channel":"IN_APP","title":"A","body":"1","dedupKey":"EVT-1"}
                """.formatted(userId);

        mockMvc.perform(post("/api/v1/notifications").header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/notifications").header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isUnauthorized());
    }

    private String crearNotificacion() throws Exception {
        return crearNotificacionConCanal("IN_APP");
    }

    private String crearNotificacionConCanal(String channel) throws Exception {
        String body = mockMvc.perform(post("/api/v1/notifications")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"customerId":"%s","channel":"%s","title":"Título","body":"Cuerpo"}
                                """.formatted(userId, channel)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body).get("id").asText();
    }
}