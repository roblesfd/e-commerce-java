package org.fernandodev.ecommerce.customer.adapter.in.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@AutoConfigureMockMvc
class CustomerControllerTest extends AbstractIntegrationTest {

    private static final String SECRET = "changemechangemechangemechangeme";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registrarClienteConTokenDevuelve201() throws Exception {
        String email = email();
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.id").isNotEmpty());
    }

    @Test
    void registrarClienteConEmailInvalidoDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + jwt())
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
        String token = jwt();
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson(email())))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/customers/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana"));
    }

    @Test
    void agregarDireccionLaEstableceComoPredeterminada() throws Exception {
        String token = jwt();
        mockMvc.perform(post("/api/v1/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(perfilJson(email())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/customers/me/addresses")
                        .header("Authorization", "Bearer " + token)
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

    private static String jwt() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .subject(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}