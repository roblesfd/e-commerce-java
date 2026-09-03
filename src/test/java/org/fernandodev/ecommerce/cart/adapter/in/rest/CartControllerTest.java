package org.fernandodev.ecommerce.cart.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
import org.fernandodev.ecommerce.inventory.application.port.out.InventoryRepository;
import org.fernandodev.ecommerce.inventory.domain.Inventory;
import org.fernandodev.ecommerce.inventory.domain.value_object.InventoryId;
import org.fernandodev.ecommerce.pricing.application.port.out.PriceRepository;
import org.fernandodev.ecommerce.pricing.domain.Price;
import org.fernandodev.ecommerce.pricing.domain.value_object.PriceId;
import org.fernandodev.ecommerce.promotion.application.port.out.CouponRepository;
import org.fernandodev.ecommerce.promotion.application.port.out.PromotionRepository;
import org.fernandodev.ecommerce.promotion.domain.Coupon;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.CouponId;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
import org.fernandodev.ecommerce.shared.domain.Money;
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
class CartControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private PriceRepository prices;

    @Autowired
    private InventoryRepository inventories;

    @Autowired
    private ProductRepository products;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private CouponRepository coupons;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of(), Set.of(), Set.of())));
    }

    @Test
    void crearCarritoDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/carts")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void obtenerCarritoVacioDevuelve200() throws Exception {
        autenticarComo();

        mockMvc.perform(get("/api/v1/carts/me")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void anadirItemDevuelve200ConSnapshotDePrecio() throws Exception {
        autenticarComo();
        UUID productId = productoConPrecioYStock();

        mockMvc.perform(post("/api/v1/carts/me/items")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","quantity":2}
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productId").value(productId.toString()))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice.amount").value(900.00))
                .andExpect(jsonPath("$.subtotal.amount").value(1800.00));
    }

    @Test
    void anadirItemSinPrecioDevuelve404() throws Exception {
        autenticarComo();
        UUID productId = UUID.randomUUID();
        inventario(productId, 10);

        mockMvc.perform(post("/api/v1/carts/me/items")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","quantity":1}
                                """.formatted(productId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void anadirItemSinStockDevuelve422() throws Exception {
        autenticarComo();
        UUID productId = UUID.randomUUID();
        precio(productId, 900);
        inventario(productId, 1);

        mockMvc.perform(post("/api/v1/carts/me/items")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","quantity":5}
                                """.formatted(productId)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void actualizarCantidadDevuelve200() throws Exception {
        autenticarComo();
        UUID productId = productoConPrecioYStock();
        String itemId = anadirItem(productId);

        mockMvc.perform(put("/api/v1/carts/me/items/{itemId}", itemId)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":4}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(4));
    }

    @Test
    void eliminarItemDevuelve200() throws Exception {
        autenticarComo();
        UUID productId = productoConPrecioYStock();
        String itemId = anadirItem(productId);

        mockMvc.perform(delete("/api/v1/carts/me/items/{itemId}", itemId)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void vaciarCarritoDevuelve200() throws Exception {
        autenticarComo();
        UUID productId = productoConPrecioYStock();
        anadirItem(productId);

        mockMvc.perform(delete("/api/v1/carts/me")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void totalesDevuelveSubtotalYTotal() throws Exception {
        autenticarComo();
        UUID productId = productoConPrecioYStock();
        anadirItem(productId);

        mockMvc.perform(get("/api/v1/carts/me/totals")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal.amount").value(1800.00))
                .andExpect(jsonPath("$.discounts.amount").value(0))
                .andExpect(jsonPath("$.total.amount").value(1800.00));
    }

    @Test
    void validarCarritoDevuelve200() throws Exception {
        autenticarComo();
        UUID productId = productoConPrecioYStock();
        anadirItem(productId);

        mockMvc.perform(post("/api/v1/carts/me/validate")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void expirarCarritoDevuelve204() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/carts/me/expire")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void aplicarCuponDevuelve200YDescuentaElTotal() throws Exception {
        autenticarComo();
        UUID productId = productoConPrecioStockYCatalogo();
        String code = guardarCupon();
        anadirItem(productId);

        mockMvc.perform(post("/api/v1/carts/me/coupons")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s"}
                                """.formatted(code)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coupon.code").value(code))
                .andExpect(jsonPath("$.coupon.discount.amount").value(180.00))
                .andExpect(jsonPath("$.total.amount").value(1620.00));
    }

    @Test
    void eliminarCuponDevuelve200YRestauraElTotal() throws Exception {
        autenticarComo();
        UUID productId = productoConPrecioStockYCatalogo();
        String code = guardarCupon();
        anadirItem(productId);
        mockMvc.perform(post("/api/v1/carts/me/coupons")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s"}
                                """.formatted(code)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/v1/carts/me/coupons")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coupon").doesNotExist())
                .andExpect(jsonPath("$.total.amount").value(1800.00));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/carts/me"))
                .andExpect(status().isUnauthorized());
    }

    private UUID productoConPrecioYStock() {
        UUID productId = UUID.randomUUID();
        precio(productId, 900);
        inventario(productId, 10);
        return productId;
    }

    private UUID productoConPrecioStockYCatalogo() {
        UUID productId = productoConPrecioYStock();
        products.save(Product.create(ProductId.of(productId),
                new Sku("SKU-" + UUID.randomUUID().toString().substring(0, 8)), null, "Producto", null, null));
        return productId;
    }

    private String guardarCupon() {
        Promotion promotion = promotions.save(Promotion.create(PromotionId.generate(), "10%",
                DiscountType.PERCENTAGE, Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2099-12-31T00:00:00Z"), new BigDecimal("10"), null, List.of(), 0, 0,
                null, null, 0, 0, Set.of(), Set.of()));
        coupons.save(Coupon.create(CouponId.generate(), "WELCOME10", promotion.getId(),
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2099-12-31T00:00:00Z")));
        return "WELCOME10";
    }

    private void precio(UUID productId, int amount) {
        prices.save(Price.define(PriceId.generate(), ProductId.of(productId), null,
                Money.of(String.valueOf(amount), "USD")));
    }

    private void inventario(UUID productId, int onHand) {
        inventories.save(Inventory.define(InventoryId.generate(), ProductId.of(productId), null, onHand, 0));
    }

    private String anadirItem(UUID productId) throws Exception {
        String body = mockMvc.perform(post("/api/v1/carts/me/items")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","quantity":2}
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode items = jsonMapper.readTree(body).get("items");
        return items.get(0).get("id").asText();
    }
}