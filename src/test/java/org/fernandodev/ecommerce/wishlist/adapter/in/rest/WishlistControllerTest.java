package org.fernandodev.ecommerce.wishlist.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.application.dto.ProductCommand;
import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.inventory.application.dto.IncreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
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
class WishlistControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private ProductUseCases products;

    @Autowired
    private InventoryUseCases inventory;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of(), Set.of(), Set.of())));
    }

    @Test
    void crearWishlistDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/wishlists")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mi lista"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Mi lista"))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void listarMisWishlistsDevuelveLista() throws Exception {
        autenticarComo();
        crearWishlist();

        mockMvc.perform(get("/api/v1/wishlists")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void renombrarWishlistDevuelve200() throws Exception {
        autenticarComo();
        String id = crearWishlist();

        mockMvc.perform(patch("/api/v1/wishlists/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Favoritos"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Favoritos"));
    }

    @Test
    void eliminarWishlistDevuelve204() throws Exception {
        autenticarComo();
        String id = crearWishlist();

        mockMvc.perform(delete("/api/v1/wishlists/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void anadirItemProductoDevuelve201() throws Exception {
        autenticarComo();
        String id = crearWishlist();
        UUID productId = crearProducto().value();

        mockMvc.perform(post("/api/v1/wishlists/{id}/items", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s"}
                                """.formatted(productId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void anadirItemVarianteDevuelve201() throws Exception {
        autenticarComo();
        String id = crearWishlist();
        UUID productId = crearProductoConStock().value();

        mockMvc.perform(post("/api/v1/wishlists/{id}/items", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","variantId":"%s"}
                                """.formatted(productId, varianteDe(productId).value())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void anadirItemDuplicadoDevuelve422() throws Exception {
        autenticarComo();
        String id = crearWishlist();
        UUID productId = crearProducto().value();
        String body = "{\"productId\":\"%s\"}".formatted(productId);

        mockMvc.perform(post("/api/v1/wishlists/{id}/items", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/wishlists/{id}/items", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void anadirProductoInexistenteDevuelve404() throws Exception {
        autenticarComo();
        String id = crearWishlist();

        mockMvc.perform(post("/api/v1/wishlists/{id}/items", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s"}
                                """.formatted(ProductId.generate().value())))
                .andExpect(status().isNotFound());
    }

    @Test
    void anadirVarianteSinStockDevuelve422() throws Exception {
        autenticarComo();
        String id = crearWishlist();
        UUID productId = crearProductoConVariante().value();

        mockMvc.perform(post("/api/v1/wishlists/{id}/items", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","variantId":"%s"}
                                """.formatted(productId, varianteDe(productId).value())))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void quitarYVaciarWishlistDevuelve204() throws Exception {
        autenticarComo();
        String id = crearWishlist();
        UUID productId = crearProducto().value();
        mockMvc.perform(post("/api/v1/wishlists/{id}/items", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"%s\"}".formatted(productId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/wishlists/{id}/items/{productId}", id, productId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/wishlists/{id}/items", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productId\":\"%s\"}".formatted(productId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/wishlists/{id}/items", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/wishlists"))
                .andExpect(status().isUnauthorized());
    }

    private String crearWishlist() throws Exception {
        String body = mockMvc.perform(post("/api/v1/wishlists")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Mi lista"}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body).get("id").asText();
    }

    private ProductId crearProducto() {
        return crearProductoConVariante();
    }

    private ProductId crearProductoConVariante() {
        Product product = products.create(new ProductCommand(
                "SKU-1", null, "Camisa", null, null, Set.of(),
                List.of(new ProductCommand.VariantCommand("SKU-1-S", null, List.of())),
                List.of(), List.of()));
        products.activate(product.getId());
        return product.getId();
    }

    private ProductId crearProductoConStock() {
        ProductId productId = crearProductoConVariante();
        inventory.increaseStock(productId, varianteDe(productId.value()),
                new IncreaseStockCommand(10, "seed"));
        return productId;
    }

    private ProductVariantId varianteDe(UUID productId) {
        return products.getById(ProductId.of(productId)).getVariants().get(0).id();
    }
}