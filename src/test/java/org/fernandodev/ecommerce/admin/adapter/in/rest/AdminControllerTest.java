package org.fernandodev.ecommerce.admin.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
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
class AdminControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-admin";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private TokenValidator tokenValidator;

    private UUID adminId;

    private void autenticarConRolAdmin() {
        adminId = UUID.randomUUID();
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(adminId, "admin", true, true, Set.of(),
                        Set.of(org.fernandodev.ecommerce.admin.security.AdminRoles.ADMIN_ROLE),
                        Set.of(org.fernandodev.ecommerce.admin.security.AdminRoles.ADMIN_GROUP))));
    }

    private void autenticarSinRolAdmin() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true,
                        Set.of(), Set.of(), Set.of())));
    }

    @Test
    void accesoConRolAdminDevuelve200() throws Exception {
        autenticarConRolAdmin();

        mockMvc.perform(get("/api/v1/admin/config")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void accesoSinRolAdminDevuelve403() throws Exception {
        autenticarSinRolAdmin();

        mockMvc.perform(get("/api/v1/admin/config")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void accesoConRolPeroSinGrupoDevuelve403() throws Exception {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "admin", true, true,
                        Set.of(), Set.of("ADMINISTRADOR"), Set.of())));

        mockMvc.perform(get("/api/v1/admin/config")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void gestionarConfigDevuelve200() throws Exception {
        autenticarConRolAdmin();

        mockMvc.perform(put("/api/v1/admin/config/order.returnWindowDays")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value":"30"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("30"));

        mockMvc.perform(get("/api/v1/admin/config/order.returnWindowDays")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("30"));
    }

    @Test
    void eliminarProductoRegistraAuditoria() throws Exception {
        autenticarConRolAdmin();
        String productId = crearProducto();

        mockMvc.perform(delete("/api/v1/admin/products/{id}", productId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/audit")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("DELETE_PRODUCT"))
                .andExpect(jsonPath("$[0].actorId").value(adminId.toString()));
    }

    @Test
    void metricasDevuelvenConteos() throws Exception {
        autenticarConRolAdmin();
        crearProducto();

        mockMvc.perform(get("/api/v1/admin/operations/metrics")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products").value(
                        org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.customers").isNumber())
                .andExpect(jsonPath("$.pendingReviews").isNumber());
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/config"))
                .andExpect(status().isUnauthorized());
    }

    private String crearProducto() throws Exception {
        String body = mockMvc.perform(post("/api/v1/admin/products")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"SKU-1","name":"Camisa","categoryIds":[],"variants":[],"attributes":[],"images":[]}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body).get("id").get("value").asText();
    }
}