# ADR-0004: Persistencia con PostgreSQL + Flyway por contexto y tests con Testcontainers

- Estado: Aceptado
- Fecha: 2026-09-01
- Contexto: PostgreSQL es la fuente de verdad transaccional; los cambios de esquema deben versionarse con Flyway; los tests de integración requieren una base real.
- Decisión:
  - PostgreSQL como fuente de verdad; Flyway gestiona el esquema con migraciones separadas por contexto (`db/migration/{contexto}/`).
  - `ddl-auto=none` y `open-in-view=false`: el esquema lo controla exclusivamente Flyway.
  - Tests de integración con **Testcontainers** usando un **único contenedor PostgreSQL singleton** compartido por todas las clases de integración (evita puertos muertos por caché de contexto de Spring).
- Consecuencias:
  - Cada bounded context versiona su propio esquema en su carpeta de migraciones.
  - Si Docker no está disponible, los tests de integración se omiten (no fallan) vía `@Testcontainers(disabledWithoutDocker = true)`.
  - Redis/Kafka/MongoDB se introducirán de forma progresiva cuando cada contexto lo justifique (no forman parte de esta fase).
