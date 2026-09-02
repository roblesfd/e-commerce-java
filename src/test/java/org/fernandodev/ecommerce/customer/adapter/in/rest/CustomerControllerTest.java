package org.fernandodev.ecommerce.customer.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

@AutoConfigureMockMvc
class CustomerControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo(UUID userId) {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(userId, "ana", true, true, Set.of())));
    }

    @Test
    void registrarClienteConTokenDevuelve201() throws Exception {
        autenticarComo(UUID.randomUUID());
        String email = email();
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void registrarClienteConEmailInvalidoDevuelve400() throws Exception {
        autenticarComo(UUID.randomUUID());
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ana","lastName":"García","email":"mal","phoneNumber":null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void accesoSinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/v1/customers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrarYConsultarMeDevuelvePerfil() throws Exception {
        autenticarComo(UUID.randomUUID());
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson(email())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/customers/me")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana"));
    }

    @Test
    void agregarDireccionLaEstableceComoPredeterminada() throws Exception {
        autenticarComo(UUID.randomUUID());
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson(email())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/customers/me/addresses")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"street":"Calle 1","city":"Madrid","state":null,"postalCode":"28001","country":"España"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.addresses.length()").value(1))
                .andExpect(jsonPath("$.defaultAddressId").isNotEmpty());
    }

    private static String perfilJson(String email) {
        return """
                {"firstName":"Ana","lastName":"García","email":"%s","phoneNumber":null}
                """.formatted(email);
    }

    private static String email() {
        return "ana." + UUID.randomUUID() + "@example.com";
    }
}