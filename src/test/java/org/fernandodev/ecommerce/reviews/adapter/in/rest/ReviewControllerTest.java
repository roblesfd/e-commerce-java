package org.fernandodev.ecommerce.reviews.adapter.in.rest;

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
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.customer.application.dto.AddressCommand;
import org.fernandodev.ecommerce.customer.application.dto.Profile;
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
import org.fernandodev.ecommerce.order.application.dto.CreateOrderCommand;
import org.fernandodev.ecommerce.order.application.dto.OrderItemCommand;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
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

import tools.jackson.databind.json.JsonMapper;

@AutoConfigureMockMvc
@Transactional
class ReviewControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private CustomerUseCases customers;

    @Autowired
    private OrderUseCases orders;

    @MockitoBean
    private TokenValidator tokenValidator;

    private UUID userId;
    private UUID addressId;

    private void prepararCliente() {
        userId = UUID.randomUUID();
        autenticarComo(userId);
        customers.register(userId, new Profile("Ana", "García", "ana@example.com", null));
        customers.addAddress(userId, new AddressCommand("Calle 1", "Madrid", null, "28001", "España"));
        addressId = customers.getByIamUserId(userId).getAddresses().get(0).id().value();
    }

    private void autenticarComo(UUID uid) {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(uid, "ana", true, true, Set.of(), Set.of(), Set.of())));
    }

    private void crearOrdenEntregada(ProductId productId) {
        UUID orderId = orders.create(new CreateOrderCommand(userId, addressId, "Calle 1, Madrid, España", null,
                List.of(new OrderItemCommand(productId, null, "CAMISA", "Camisa", 1, usd(900))),
                usd(900), usd(0), usd(900))).getId().value();
        OrderId id = OrderId.of(orderId);
        orders.confirm(id);
        orders.process(id);
        orders.markReadyToShip(id);
        orders.markShipped(id);
        orders.markDelivered(id);
    }

    @Test
    void crearReviewParaProductoCompradoDevuelve201() throws Exception {
        prepararCliente();
        ProductId productId = ProductId.generate();
        crearOrdenEntregada(productId);

        mockMvc.perform(post("/api/v1/reviews")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","rating":5,"title":"Excelente","comment":"Muy buen producto"}
                                """.formatted(productId.value())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_MODERATION"))
                .andExpect(jsonPath("$.verifiedPurchase").value(true))
                .andExpect(jsonPath("$.rating").value(5));
    }

    @Test
    void crearReviewDuplicadoDevuelve409() throws Exception {
        prepararCliente();
        ProductId productId = ProductId.generate();
        String body = "{\"productId\":\"%s\",\"rating\":5,\"comment\":\"Bueno\"}".formatted(productId.value());

        mockMvc.perform(post("/api/v1/reviews").header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/v1/reviews").header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void crearReviewConRatingInvalidoDevuelve400() throws Exception {
        prepararCliente();

        mockMvc.perform(post("/api/v1/reviews")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","rating":6,"comment":"Invalido"}
                                """.formatted(ProductId.generate().value())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aprobarYListarReviewPublicadoDevuelve200() throws Exception {
        prepararCliente();
        ProductId productId = ProductId.generate();
        String id = crearReview(productId, "Muy bueno");

        mockMvc.perform(post("/api/v1/reviews/{id}/approve", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("PUBLISHED"));

        mockMvc.perform(get("/api/v1/products/{productId}/reviews", productId.value())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"));
    }

    @Test
    void ratingDevuelvePromedioYDistribucion() throws Exception {
        prepararCliente();
        ProductId productId = ProductId.generate();

        String a = crearReview(productId, "Excelente");
        mockMvc.perform(post("/api/v1/reviews/{id}/approve", a)
                .header("Authorization", "Bearer " + TOKEN)).andExpect(status().isOk());

        autenticarComo(UUID.randomUUID());
        String b = crearReview(productId, 4, "Bueno");
        mockMvc.perform(post("/api/v1/reviews/{id}/approve", b)
                .header("Authorization", "Bearer " + TOKEN)).andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/products/{productId}/rating", productId.value())
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.average").value(4.50))
                .andExpect(jsonPath("$.distribution['5']").value(1))
                .andExpect(jsonPath("$.distribution['4']").value(1));
    }

    @Test
    void responderReviewPublicadoDevuelve200() throws Exception {
        prepararCliente();
        ProductId productId = ProductId.generate();
        String id = crearReview(productId, "Muy bueno");
        mockMvc.perform(post("/api/v1/reviews/{id}/approve", id)
                .header("Authorization", "Bearer " + TOKEN)).andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/reviews/{id}/replies", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"comment":"Gracias por tu opinión"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.replies.length()").value(1));
    }

    @Test
    void listarMisReviewsDevuelveLista() throws Exception {
        prepararCliente();
        ProductId productId = ProductId.generate();
        crearReview(productId, "Muy bueno");

        mockMvc.perform(get("/api/v1/reviews/me")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void moderacionDevuelvePendientes() throws Exception {
        prepararCliente();
        ProductId productId = ProductId.generate();
        String id = crearReview(productId, "Muy bueno");

        mockMvc.perform(get("/api/v1/reviews")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING_MODERATION"));

        mockMvc.perform(delete("/api/v1/reviews/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isNoContent());
    }

    @Test
    void actualizarReviewPublicadoVuelveAPendiente() throws Exception {
        prepararCliente();
        ProductId productId = ProductId.generate();
        String id = crearReview(productId, "Muy bueno");
        mockMvc.perform(post("/api/v1/reviews/{id}/approve", id)
                .header("Authorization", "Bearer " + TOKEN)).andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/reviews/{id}", id)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"rating":4,"title":"Actualizado","comment":"Nuevo comentario"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_MODERATION"))
                .andExpect(jsonPath("$.rating").value(4));
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/reviews/me"))
                .andExpect(status().isUnauthorized());
    }

    private String crearReview(ProductId productId, String comment) throws Exception {
        return crearReview(productId, 5, comment);
    }

    private String crearReview(ProductId productId, int rating, String comment) throws Exception {
        String body = mockMvc.perform(post("/api/v1/reviews")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productId":"%s","rating":%d,"comment":"%s"}
                                """.formatted(productId.value(), rating, comment)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(body).get("id").asText();
    }

    private static Money usd(int amount) {
        return Money.of(String.valueOf(amount), "USD");
    }
}