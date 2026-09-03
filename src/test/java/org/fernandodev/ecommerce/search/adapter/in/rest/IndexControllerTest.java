package org.fernandodev.ecommerce.search.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.application.dto.ProductCommand;
import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.pricing.application.dto.DefinePriceCommand;
import org.fernandodev.ecommerce.pricing.application.port.in.PricingUseCases;
import org.fernandodev.starter.iam.client.TokenValidator;
import org.fernandodev.starter.iam.dto.ValidarTokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@AutoConfigureMockMvc
@Transactional
class IndexControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductUseCases products;

    @Autowired
    private PricingUseCases pricing;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of(), Set.of(), Set.of())));
    }

    @Test
    void indexarProductoDevuelveSyncronizado() throws Exception {
        autenticarComo();
        ProductId productId = crearProducto();

        mockMvc.perform(post("/api/v1/search/index/products/{productId}", productId.value())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexStatus").value("SYNCED"));
    }

    @Test
    void indexarProductoInexistenteMarcaFailed() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/search/index/products/{productId}", ProductId.generate().value())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.indexStatus").value("FAILED"))
                .andExpect(jsonPath("$.lastError").isNotEmpty());
    }

    @Test
    void eliminarDelIndiceDevuelve204() throws Exception {
        autenticarComo();
        ProductId productId = crearProducto();

        mockMvc.perform(delete("/api/v1/search/index/products/{productId}", productId.value())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void rebuildDevuelve200() throws Exception {
        autenticarComo();
        crearProducto();

        mockMvc.perform(post("/api/v1/search/index/rebuild")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk());
    }

    @Test
    void listarFallosDevuelveLosFallidos() throws Exception {
        autenticarComo();
        crearProducto();
        mockMvc.perform(post("/api/v1/search/index/products/{productId}", ProductId.generate().value())
                        .header("Authorization", "Bearer " + TOKEN)).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/search/index/failures")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].indexStatus").value("FAILED"));
    }

    private ProductId crearProducto() {
        Product product = products.create(new ProductCommand(
                "SKU-1", null, "Camisa", null, null, Set.of(), List.of(), List.of(), List.of()));
        products.activate(product.getId());
        pricing.defineBasePrice(new DefinePriceCommand(product.getId(), null, BigDecimal.valueOf(900), "USD"));
        return product.getId();
    }
}