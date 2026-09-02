# ADR-0005: Starter IAM como librería de integración

- Estado: Aceptado
- Fecha: 2026-09-01
- Contexto: el e-commerce necesita acceder a las funcionalidades del IAM (identidad, auth, tokens, permisos, sesiones) y a sus value objects, sin duplicar funcionalidad. El IAM es un sistema externo con varios bounded contexts.
- Decisión:
  - Se construye un **módulo `starter`** dentro de `identity-platform` (jar con auto-configuración: `@AutoConfiguration` + `AutoConfiguration.imports`).
  - El starter depende del módulo `domain` del IAM, **exponiendo sus value objects y modelos como API pública** para los consumidores (vía transitividad de Maven).
  - Auto-configura (activada por `iam.base-url`) un `RestClient` hacia el IAM y los clientes: `TokenValidator` (validación remota), `AuthClient` (login/refresh/logout), `UserClient`, `PermissionClient` y `SessionClient`.
  - Se añade el endpoint `POST /auth/validar` en el IAM para la validación remota de tokens.
- Consecuencias:
  - E-Commerce consume el IAM como librería de cliente, sin acoplar sus modelos internos: define sus propios DTOs del contrato.
  - Los value objects del IAM (`Email`, `PhoneNumber`, `UsuarioId`, ...) quedan disponibles como API pública del starter.
  - **Se sustituyeron los VOs de customer** (`Email`, `PhoneNumber`) por los del IAM, aceptando el cambio de reglas de negocio: el `Email` del IAM usa el acceso `direccion()` y una validación más estricta (regex + máx 254), y el `PhoneNumber` del IAM exige **exactamente 10 dígitos (formato mexicano)**, normalizando y eliminando caracteres no numéricos. Esto altera la regla previa que aceptaba formatos internacionales.
  - El starter se publica en m2 local (`mvn install`) y se consume como dependencia `org.fernandodev:starter`.
  - Endpoints protegidos del IAM (`/usuarios/**`, `/roles`, ...) requieren bearer token de administración; el cliente los acepta por llamada.