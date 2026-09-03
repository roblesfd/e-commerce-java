package org.fernandodev.ecommerce.search.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.fernandodev.ecommerce.inventory.application.dto.IncreaseStockCommand;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.fernandodev.ecommerce.pricing.application.dto.DefinePriceCommand;
import org.fernandodev.ecommerce.pricing.application.port.in.PricingUseCases;
import org.fernandodev.ecommerce.search.application.port.in.IndexUseCases;
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
class SearchControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductUseCases products;

    @Autowired
    private PricingUseCases pricing;

    @Autowired
    private InventoryUseCases inventory;

    @Autowired
    private IndexUseCases index;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of(), Set.of(), Set.of())));
    }

    @Test
    void buscarPorTextoDevuelveResultados() throws Exception {
        autenticarComo();
        crearProducto("Camisa", "SKU-1", 900, 5);
        crearProducto("Zapatos", "SKU-2", 300, 0);

        mockMvc.perform(get("/api/v1/search/products").param("q", "camisa")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Camisa"))
                .andExpect(jsonPath("$.items[0].available").value(true));
    }

    @Test
    void filtrarPorDisponibilidadDevuelveSoloDisponibles() throws Exception {
        autenticarComo();
        crearProducto("Camisa", "SKU-1", 900, 5);
        crearProducto("Zapatos", "SKU-2", 300, 0);

        mockMvc.perform(get("/api/v1/search/products").param("available", "true")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].name").value("Camisa"));
    }

    @Test
    void filtrarPorRangoDePrecioDevuelveResultados() throws Exception {
        autenticarComo();
        crearProducto("Camisa", "SKU-1", 900, 5);
        crearProducto("Zapatos", "SKU-2", 300, 0);

        mockMvc.perform(get("/api/v1/search/products").param("minPrice", "500")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.items[0].price").value(900.00));
    }

    @Test
    void ordenarPorPrecioDescendenteDevuelveResultados() throws Exception {
        autenticarComo();
        crearProducto("Camisa", "SKU-1", 900, 5);
        crearProducto("Zapatos", "SKU-2", 300, 0);

        mockMvc.perform(get("/api/v1/search/products").param("sort", "PRICE_DESC")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Camisa"));
    }

    @Test
    void autocompleteDevuelveNombres() throws Exception {
        autenticarComo();
        crearProducto("Camisa", "SKU-1", 900, 5);

        mockMvc.perform(get("/api/v1/search/autocomplete").param("q", "cami")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Camisa"));
    }

    @Test
    void suggestDevuelveNombres() throws Exception {
        autenticarComo();
        crearProducto("Zapatos de cuero", "SKU-2", 300, 5);

        mockMvc.perform(get("/api/v1/search/suggest").param("q", "zapa")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Zapatos de cuero"));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/search/products"))
                .andExpect(status().isUnauthorized());
    }

    private void crearProducto(String name, String sku, int price, int stock) {
        Product product = products.create(new ProductCommand(
                sku, null, name, null, null, Set.of(), List.of(), List.of(), List.of()));
        products.activate(product.getId());
        pricing.defineBasePrice(new DefinePriceCommand(product.getId(), null, BigDecimal.valueOf(price), "USD"));
        if (stock > 0) {
            inventory.increaseStock(product.getId(), null, new IncreaseStockCommand(stock, "seed"));
        }
        index.indexProduct(product.getId());
    }
}