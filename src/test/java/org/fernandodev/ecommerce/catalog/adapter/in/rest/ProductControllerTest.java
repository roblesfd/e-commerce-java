package org.fernandodev.ecommerce.catalog.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.fernandodev.ecommerce.catalog.application.port.out.BrandRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.CategoryRepository;
import org.fernandodev.ecommerce.catalog.domain.Brand;
import org.fernandodev.ecommerce.catalog.domain.Category;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
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
class ProductControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BrandRepository brands;

    @Autowired
    private CategoryRepository categories;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of())));
    }

    @Test
    void crearProductoConDetallesDevuelve201() throws Exception {
        autenticarComo();
        UUID marca = brand();
        UUID categoria = category();
        String sku = sku();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson(sku, marca, categoria)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(sku.toUpperCase()))
                .andExpect(jsonPath("$.name").value("Camisa negra"))
                .andExpect(jsonPath("$.brandId").value(marca.toString()))
                .andExpect(jsonPath("$.categoryIds[0]").value(categoria.toString()))
                .andExpect(jsonPath("$.variants[0].sku").value(sku.toUpperCase() + "-S"))
                .andExpect(jsonPath("$.attributes[0].name").value("Color"))
                .andExpect(jsonPath("$.images[0]").value("https://img.example.com/a.jpg"));
    }

    @Test
    void crearProductoConNombreVacioDevuelve400() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"CAMISA","name":"","description":null,"brandId":null,
                                 "categoryIds":[],"variants":[],"attributes":[],"images":[]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearProductoConSkuDuplicadoDevuelve409() throws Exception {
        autenticarComo();
        String sku = sku();
        crearProducto(sku);

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson(sku, null, null)))
                .andExpect(status().isConflict());
    }

    @Test
    void crearProductoConMarcaInexistenteDevuelve404() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson(sku(), UUID.randomUUID(), null)))
                .andExpect(status().isNotFound());
    }

    @Test
    void obtenerProductoPorIdDevuelve200() throws Exception {
        autenticarComo();
        String sku = sku();
        String id = crearProducto(sku);

        mockMvc.perform(get("/api/v1/products/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value(sku.toUpperCase()));
    }

    @Test
    void obtenerProductoInexistenteDevuelve404() throws Exception {
        autenticarComo();

        mockMvc.perform(get("/api/v1/products/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarProductosFiltraYPagina() throws Exception {
        autenticarComo();
        UUID categoria = category();
        String skuActivo = sku();
        crearProductoEnCategoria(skuActivo, categoria);
        crearProducto(sku());

        mockMvc.perform(get("/api/v1/products")
                        .param("active", "true")
                        .param("categoryId", categoria.toString())
                        .param("page", "0")
                        .param("size", "10")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].sku").value(skuActivo.toUpperCase()));
    }

    @Test
    void actualizarProductoDevuelve200() throws Exception {
        autenticarComo();
        String id = crearProducto(sku());

        mockMvc.perform(put("/api/v1/products/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku":"%s","name":"Camisa 2026","description":"Nueva",
                                 "brandId":null,"categoryIds":[],"variants":[],"attributes":[],"images":[]}
                                """.formatted(sku())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Camisa 2026"));
    }

    @Test
    void activarYDesactivarCambianElEstado() throws Exception {
        autenticarComo();
        String id = crearProducto(sku());

        mockMvc.perform(patch("/api/v1/products/{id}/deactivate", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch("/api/v1/products/{id}/activate", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void eliminarProductoDevuelve204YDejaDeExistir() throws Exception {
        autenticarComo();
        String id = crearProducto(sku());

        mockMvc.perform(delete("/api/v1/products/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/products/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isUnauthorized());
    }

    private String crearProducto(String sku) throws Exception {
        return crearProductoEnCategoria(sku, null);
    }

    private String crearProductoEnCategoria(UUID categoria) throws Exception {
        return crearProductoEnCategoria(sku(), categoria);
    }

    private String crearProductoEnCategoria(String sku, UUID categoria) throws Exception {
        String body = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson(sku, null, categoria)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return body.substring(body.indexOf("\"id\":\"") + 6, body.indexOf("\"", body.indexOf("\"id\":\"") + 6));
    }

    private UUID brand() {
        return brands.save(Brand.create(BrandId.generate(), "Nike")).getId().value();
    }

    private UUID category() {
        return categories.save(Category.create(CategoryId.generate(), "Ropa", null)).getId().value();
    }

    private static String sku() {
        return "SKU-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String productoJson(String sku, UUID marca, UUID categoria) {
        String barcode = "750" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        return """
                {
                  "sku": "%s",
                  "barcode": "%s",
                  "name": "Camisa negra",
                  "description": "Camisa de algodón",
                  "brandId": %s,
                  "categoryIds": %s,
                  "variants": [{"sku": "%s-S", "barcode": null, "attributes": [{"name":"Talla","value":"S"}]}],
                  "attributes": [{"name":"Color","value":"Negro"}],
                  "images": ["https://img.example.com/a.jpg"]
                }
                """.formatted(
                sku,
                barcode,
                marca == null ? "null" : "\"" + marca + "\"",
                categoria == null ? "[]" : "[\"" + categoria + "\"]",
                sku);
    }
}