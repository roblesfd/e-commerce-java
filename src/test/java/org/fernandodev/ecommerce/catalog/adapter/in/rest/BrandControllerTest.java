package org.fernandodev.ecommerce.catalog.adapter.in.rest;

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
import org.fernandodev.ecommerce.catalog.application.port.out.BrandRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
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
class BrandControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrandRepository brands;

    @Autowired
    private ProductRepository products;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of())));
    }

    @Test
    void crearMarcaDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/brands")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Nike"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Nike"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void crearMarcaConNombreVacioDevuelve400() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/brands")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerMarcaPorIdDevuelve200() throws Exception {
        autenticarComo();
        UUID id = crearMarca("Nike");

        mockMvc.perform(get("/api/v1/brands/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nike"));
    }

    @Test
    void listarMarcasDevuelveLista() throws Exception {
        autenticarComo();
        crearMarca("Nike");
        crearMarca("Adidas");

        mockMvc.perform(get("/api/v1/brands")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void actualizarMarcaDevuelve200() throws Exception {
        autenticarComo();
        UUID id = crearMarca("Nike");

        mockMvc.perform(put("/api/v1/brands/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Adidas"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Adidas"));
    }

    @Test
    void eliminarMarcaConProductosDevuelve422() throws Exception {
        autenticarComo();
        UUID id = crearMarca("Nike");
        products.save(Product.create(ProductId.generate(), new Sku("CAMISA-" + UUID.randomUUID().toString().substring(0, 8)),
                null, "Camisa", null, BrandId.of(id)));

        mockMvc.perform(delete("/api/v1/brands/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void eliminarMarcaSinProductosDevuelve204() throws Exception {
        autenticarComo();
        UUID id = crearMarca("Nike");

        mockMvc.perform(delete("/api/v1/brands/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/brands/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/brands"))
                .andExpect(status().isUnauthorized());
    }

    private UUID crearMarca(String name) throws Exception {
        String body = mockMvc.perform(post("/api/v1/brands")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s"}
                                """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(body.substring(body.indexOf("\"id\":\"") + 6, body.indexOf("\"", body.indexOf("\"id\":\"") + 6)));
    }
}