package org.fernandodev.ecommerce;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Clase base para tests de integración.
 * <p>
 * Levanta PostgreSQL y Redis (Testcontainers) compartidos por todas las clases
 * de integración y los conecta al contexto de Spring mediante
 * {@link DynamicPropertySource}. Flyway se ejecuta contra Postgres en cada
 * contexto de test. Si Docker no está disponible, los tests se omiten en lugar
 * de fallar.
 * </p>
 */
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    static final GenericContainer<?> REDIS =
            new GenericContainer<>("redis:7-alpine")
                    .withExposedPorts(6379);

    static {
        if (DockerClientFactory.instance().isDockerAvailable()) {
            POSTGRES.start();
            REDIS.start();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                POSTGRES.stop();
                REDIS.stop();
            }));
        }
    }

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }
}