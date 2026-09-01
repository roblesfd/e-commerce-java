# ADR-0002: Java 21 y Spring Boot 4.1.x

- Estado: Aceptado
- Fecha: 2026-09-01
- Contexto: el spec fija Java 21 como lenguaje principal y Spring Boot como runtime. Se elige la versión de Spring Boot en un proyecto greenfield (2026).
- Decisión:
  - Java **21** (LTS), acorde al spec.
  - Spring Boot **4.1.x** (última estable) como runtime.
  - Spring Boot 4 usa **Jackson 3** (paquete `tools.jackson`); no existe bean `com.fasterxml.jackson.databind.ObjectMapper` por defecto. Se inyecta `tools.jackson.databind.json.JsonMapper`.
- Consecuencias:
  - Boot 4 reorganizó las auto-configuraciones en módulos: la de Flyway vive en `spring-boot-starter-flyway` (no basta con `flyway-core`) y el soporte MockMvc en `spring-boot-starter-webmvc-test`.
  - Boot 4 no gestiona las versiones de Testcontainers: se importa el BOM `testcontainers-bom` 1.21.x.
  - Si apareciera fricción con el ecosistema, se podría bajar a Spring Boot 3.5.x (LTS previo) manteniendo Java 21.
