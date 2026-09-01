# ADR-0003: Integración IAM mediante validación local de JWT

- Estado: Aceptado
- Fecha: 2026-09-01
- Contexto: E-Commerce depende de un IAM externo (`identity-platform`) que posee identidad, credenciales, autenticación, tokens, roles y permisos. El spec prohíbe duplicar funcionalidad del IAM y obliga a inspeccionarlo antes de integrar.
- Hallazgos del IAM:
  - Emite JWT firmados con **HS256** usando un secreto simétrico (`iam.jwt.secret`).
  - El claim relevante es `sub` = `UsuarioId` (UUID). Los roles/permisos NO van en el token: se cargan server-side por request.
- Decisión:
  - E-Commerce **valida el JWT localmente** (misma firma HS256) y extrae `sub` → principal `AuthenticatedUser(usuarioId)`.
  - **No se replican** roles ni permisos del IAM. La autorización inicial se basa en la **propiedad de los recursos** del usuario autenticado (el contexto Customer resuelve por `iamUserId` del token).
  - Spring Security stateless; rutas públicas: documentación OpenAPI y `actuator/health`.
- Consecuencias:
  - Caveat: la validación local no detecta **revocación de sesión/token** del IAM en tiempo real. Aceptable para el inicio; si se requiere, se añadirá una consulta de revocación o tokens versionados.
  - Las funciones administrativas que exijan roles se resolverán consultando al IAM (p. ej. `GET /usuarios/{id}/permisos`) cuando existan endpoints admin.
