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
import org.fernandodev.ecommerce.catalog.application.port.out.CategoryRepository;
import org.fernandodev.ecommerce.catalog.domain.Category;
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
class CategoryControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categories;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of(), Set.of(), Set.of())));
    }

    @Test
    void crearCategoriaDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Electrónica","parentId":null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Electrónica"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void crearCategoriaConNombreVacioDevuelve400() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","parentId":null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void crearCategoriaConPadreDevuelve201() throws Exception {
        autenticarComo();
        UUID padre = crearCategoria("Electrónica");

        mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Laptops","parentId":"%s"}
                                """.formatted(padre)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.parentId").value(padre.toString()));
    }

    @Test
    void obtenerCategoriaInexistenteDevuelve404() throws Exception {
        autenticarComo();

        mockMvc.perform(get("/api/v1/categories/{id}", UUID.randomUUID())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void listarCategoriasDevuelveLista() throws Exception {
        autenticarComo();
        crearCategoria("Electrónica");
        crearCategoria("Ropa");

        mockMvc.perform(get("/api/v1/categories")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void actualizarCategoriaDevuelve200() throws Exception {
        autenticarComo();
        UUID id = crearCategoria("Electrónica");

        mockMvc.perform(put("/api/v1/categories/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Tecnología","parentId":null}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Tecnología"));
    }

    @Test
    void eliminarCategoriaConSubcategoriasDevuelve422() throws Exception {
        autenticarComo();
        UUID padre = crearCategoria("Electrónica");
        crearCategoria("Laptops", padre);

        mockMvc.perform(delete("/api/v1/categories/{id}", padre)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void eliminarCategoriaSinDependenciasDevuelve204() throws Exception {
        autenticarComo();
        UUID id = crearCategoria("Electrónica");

        mockMvc.perform(delete("/api/v1/categories/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/categories/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNotFound());
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized());
    }

    private UUID crearCategoria(String name) throws Exception {
        return crearCategoria(name, null);
    }

    private UUID crearCategoria(String name, UUID parentId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/categories")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s","parentId":%s}
                                """.formatted(name, parentId == null ? "null" : "\"" + parentId + "\"")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return UUID.fromString(body.substring(body.indexOf("\"id\":\"") + 6, body.indexOf("\"", body.indexOf("\"id\":\"") + 6)));
    }
}