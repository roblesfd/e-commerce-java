# ADR-0002: Java 25 y Spring Boot 4.1.x

- Estado: Aceptado
- Fecha: 2026-09-01
- Contexto: el spec fija Java como lenguaje principal y Spring Boot como runtime. Inicialmente se eligió Java 21, pero la integración con el IAM (que compila a Java 25) llevó a alinear ambos proyectos. Se modificó `specs.md` (Java 21 → Java 25).
- Decisión:
  - Java **25** como versión de bytecode (acorde al spec actualizado). No hay JDK 25 local: se compila con **JDK 26 usando `--release 25`** (bytecode compatible Java 25).
  - Spring Boot **4.1.x** (última estable) como runtime.
  - Spring Boot 4 usa **Jackson 3** (paquete `tools.jackson`); no existe bean `com.fasterxml.jackson.databind.ObjectMapper` por defecto. Se inyecta `tools.jackson.databind.json.JsonMapper`.
- Consecuencias:
  - El IAM (`identity-platform`) ya compila a Java 25, por lo que su `domain` y su `starter` son consumibles desde e-commerce sin conflictos de bytecode.
  - Boot 4 reorganizó las auto-configuraciones en módulos: la de Flyway vive en `spring-boot-starter-flyway` (no basta con `flyway-core`) y el soporte MockMvc en `spring-boot-starter-webmvc-test`.
  - Boot 4 no gestiona las versiones de Testcontainers: se importa el BOM `testcontainers-bom` 1.21.x.