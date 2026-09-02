package org.fernandodev.ecommerce.inventory.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
class InventoryControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of())));
    }

    @Test
    void aumentarStockCreaInventarioDevuelve200() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/products/{productId}/inventory/stock/increase", UUID.randomUUID())
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":10,"reason":"reposición"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(10));
    }

    @Test
    void aumentarStockConCantidadInvalidaDevuelve400() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/products/{productId}/inventory/stock/increase", UUID.randomUUID())
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":0,"reason":null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerInventarioDevuelve200() throws Exception {
        autenticarComo();
        UUID productId = crearInventario();

        mockMvc.perform(get("/api/v1/products/{productId}/inventory", productId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(10))
                .andExpect(jsonPath("$.lowStock").value(false));
    }

    @Test
    void obtenerInventarioInexistenteDevuelve404() throws Exception {
        autenticarComo();

        mockMvc.perform(get("/api/v1/products/{productId}/inventory", UUID.randomUUID())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void reservarStockDevuelve201() throws Exception {
        autenticarComo();
        UUID productId = crearInventario();

        mockMvc.perform(post("/api/v1/products/{productId}/inventory/reservations", productId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":4,"expiresAt":"2099-12-31T00:00:00Z"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(4))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void reservarMasDelDisponiblePrevieneOversellingDevuelve422() throws Exception {
        autenticarComo();
        UUID productId = crearInventario();

        mockMvc.perform(post("/api/v1/products/{productId}/inventory/reservations", productId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":15,"expiresAt":"2099-12-31T00:00:00Z"}
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void confirmarReservaDevuelve200YDescuentaStock() throws Exception {
        autenticarComo();
        UUID productId = crearInventario();
        String reservationId = reservar(productId);

        mockMvc.perform(post("/api/v1/products/{productId}/inventory/reservations/{reservationId}/confirm",
                        productId, reservationId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(6))
                .andExpect(jsonPath("$.onHand").value(6));
    }

    @Test
    void liberarReservaDevuelve200YRestauraStock() throws Exception {
        autenticarComo();
        UUID productId = crearInventario();
        String reservationId = reservar(productId);

        mockMvc.perform(post("/api/v1/products/{productId}/inventory/reservations/{reservationId}/release",
                        productId, reservationId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(10));
    }

    @Test
    void expirarReservaDevuelve200YRestauraStock() throws Exception {
        autenticarComo();
        UUID productId = crearInventario();
        String reservationId = reservar(productId);

        mockMvc.perform(post("/api/v1/products/{productId}/inventory/reservations/{reservationId}/expire",
                        productId, reservationId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(10));
    }

    @Test
    void disminuirStockDevuelve200() throws Exception {
        autenticarComo();
        UUID productId = crearInventario();

        mockMvc.perform(post("/api/v1/products/{productId}/inventory/stock/decrease", productId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":3,"reason":"venta"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(7));
    }

    @Test
    void ajustarStockDevuelve200() throws Exception {
        autenticarComo();
        UUID productId = crearInventario();

        mockMvc.perform(put("/api/v1/products/{productId}/inventory/stock", productId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"targetQuantity":25,"reason":"conteo"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(25));
    }

    @Test
    void definirStockMinimoDevuelve200() throws Exception {
        autenticarComo();
        UUID productId = crearInventario();

        mockMvc.perform(patch("/api/v1/products/{productId}/inventory/minimum-stock", productId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"minimumStock":5}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.minimumStock").value(5));
    }

    @Test
    void movimientosDevuelveLaAuditoria() throws Exception {
        autenticarComo();
        UUID productId = crearInventario();

        mockMvc.perform(get("/api/v1/products/{productId}/inventory/movements", productId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("INCREASE"));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/products/{productId}/inventory", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private UUID crearInventario() throws Exception {
        UUID productId = UUID.randomUUID();
        mockMvc.perform(post("/api/v1/products/{productId}/inventory/stock/increase", productId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":10,"reason":"reposición"}
                                """))
                .andExpect(status().isOk());
        return productId;
    }

    private String reservar(UUID productId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/products/{productId}/inventory/reservations", productId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":4,"expiresAt":"2099-12-31T00:00:00Z"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = jsonMapper.readTree(body);
        return node.get("id").asText();
    }
}