package org.fernandodev.ecommerce;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Test humo que verifica que el contexto de Spring arranca con la
 * infraestructura mínima (PostgreSQL vía Testcontainers + Flyway).
 */
class ECommerceApplicationTest extends AbstractIntegrationTest {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextoDeSpringSeCarga() {
        assertThat(context).isNotNull();
    }

    @Test
    void flywayEjecutaContraPostgres() {
        Integer historial = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM flyway_schema_history",
                Integer.class);

        assertThat(historial).isNotNull();
        assertThat(historial).isGreaterThanOrEqualTo(0);
    }
}