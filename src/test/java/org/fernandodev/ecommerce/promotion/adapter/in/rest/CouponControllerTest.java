package org.fernandodev.ecommerce.promotion.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.fernandodev.ecommerce.promotion.application.port.out.PromotionRepository;
import org.fernandodev.ecommerce.promotion.domain.Promotion;
import org.fernandodev.ecommerce.promotion.domain.value_object.DiscountType;
import org.fernandodev.ecommerce.promotion.domain.value_object.PromotionId;
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
class CouponControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";
    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant T2 = Instant.parse("2099-12-31T00:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private ProductRepository products;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of())));
    }

    @Test
    void crearCuponDevuelve201() throws Exception {
        autenticarComo();
        UUID promotionId = guardarPromocion();

        mockMvc.perform(post("/api/v1/coupons")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"WELCOME10","promotionId":"%s",
                                 "validFrom":"2026-01-01T00:00:00Z","validTo":"2099-12-31T00:00:00Z"}
                                """.formatted(promotionId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WELCOME10"));
    }

    @Test
    void crearCuponConPromocionInexistenteDevuelve404() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/coupons")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"WELCOME10","promotionId":"%s",
                                 "validFrom":"2026-01-01T00:00:00Z","validTo":"2099-12-31T00:00:00Z"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isNotFound());
    }

    @Test
    void aplicarCuponDevuelve200ConDescuento() throws Exception {
        autenticarComo();
        UUID promotionId = guardarPromocion();
        crearCupon(promotionId, "WELCOME10");
        UUID productId = guardarProducto();

        mockMvc.perform(post("/api/v1/coupons/apply")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"WELCOME10","items":[{"productId":"%s","quantity":1,
                                 "unitPrice":{"amount":1000.00,"currency":"USD"}}]}
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("WELCOME10"))
                .andExpect(jsonPath("$.discount.amount").value(100.00));
    }

    @Test
    void aplicarCuponNoElegibleDevuelve422() throws Exception {
        autenticarComo();
        UUID promotionId = guardarPromocion(Set.of(ProductId.generate()));
        crearCupon(promotionId, "SCOPED10");
        UUID productId = guardarProducto();

        mockMvc.perform(post("/api/v1/coupons/apply")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"SCOPED10","items":[{"productId":"%s","quantity":1,
                                 "unitPrice":{"amount":1000.00,"currency":"USD"}}]}
                                """.formatted(productId)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void validarCuponDevuelve200ConResultado() throws Exception {
        autenticarComo();
        UUID promotionId = guardarPromocion();
        crearCupon(promotionId, "WELCOME10");
        UUID productId = guardarProducto();

        mockMvc.perform(post("/api/v1/coupons/validate")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"WELCOME10","items":[{"productId":"%s","quantity":1,
                                 "unitPrice":{"amount":1000.00,"currency":"USD"}}]}
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(post("/api/v1/coupons/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"X","items":[]}
                                """))
                .andExpect(status().isUnauthorized());
    }

    private UUID guardarPromocion() {
        return guardarPromocion(Set.of());
    }

    private UUID guardarPromocion(Set<ProductId> productIds) {
        return promotions.save(Promotion.create(PromotionId.generate(), "10%", DiscountType.PERCENTAGE,
                T1, T2, new BigDecimal("10"), null, List.of(), 0, 0, null, null, 0, 0,
                productIds, Set.of())).getId().value();
    }

    private void crearCupon(UUID promotionId, String code) throws Exception {
        mockMvc.perform(post("/api/v1/coupons")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","promotionId":"%s",
                                 "validFrom":"2026-01-01T00:00:00Z","validTo":"2099-12-31T00:00:00Z"}
                                """.formatted(code, promotionId)))
                .andExpect(status().isCreated());
    }

    private UUID guardarProducto() {
        return products.save(Product.create(ProductId.generate(),
                new Sku("SKU-" + UUID.randomUUID().toString().substring(0, 8)), null, "Producto", null, null))
                .getId().value();
    }
}