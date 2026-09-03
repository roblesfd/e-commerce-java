package org.fernandodev.ecommerce.returns.adapter.in.rest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.AbstractIntegrationTest;
import org.fernandodev.ecommerce.returns.application.dto.CreateReturnPolicyCommand;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnPolicyUseCases;
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
class ReturnPolicyControllerTest extends AbstractIntegrationTest {

    private static final String TOKEN = "token-de-prueba";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReturnPolicyUseCases policies;

    @MockitoBean
    private TokenValidator tokenValidator;

    private void autenticarComo() {
        when(tokenValidator.validate(anyString()))
                .thenReturn(Optional.of(new ValidarTokenResponse(UUID.randomUUID(), "ana", true, true, Set.of(), Set.of(), Set.of())));
    }

    @Test
    void crearPoliticaDevuelve201() throws Exception {
        autenticarComo();

        mockMvc.perform(post("/api/v1/return-policies")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"returnWindowDays":30,"returnableProductIds":[]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.returnWindowDays").value(30))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void listarPoliticasDevuelveLista() throws Exception {
        autenticarComo();
        policies.create(new CreateReturnPolicyCommand(30, Set.of()));

        mockMvc.perform(get("/api/v1/return-policies")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void desactivarYActivarPoliticaDevuelve200() throws Exception {
        autenticarComo();
        var id = policies.create(new CreateReturnPolicyCommand(30, Set.of())).getId().value();

        mockMvc.perform(patch("/api/v1/return-policies/{id}/deactivate", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(patch("/api/v1/return-policies/{id}/activate", id)
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }
}