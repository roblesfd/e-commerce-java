# ADR-0003: Integración IAM mediante validación remota del token

- Estado: Aceptado
- Fecha: 2026-09-01
- Contexto: E-Commerce depende de un IAM externo (`identity-platform`) que posee identidad, credenciales, autenticación, tokens, roles y permisos. El spec prohíbe duplicar funcionalidad del IAM y obliga a inspeccionarlo antes de integrar.
- Hallazgos del IAM:
  - Emite JWT firmados con **HS256**; el claim relevante es `sub` = `UsuarioId` (UUID). Los roles/permisos NO van en el token.
  - No exponía un endpoint de validación de token; se añadió **`POST /auth/validar`** (valida el JWT y devuelve identidad, estado de cuenta y permisos efectivos).
- Decisión:
  - E-Commerce **valida el token de forma remota por request** contra `POST /auth/validar` del IAM, usando el cliente `TokenValidator` del starter IAM.
  - **No se replican** roles ni permisos del IAM: el principal `AuthenticatedUser` se construye con la identidad y los **permisos efectivos** que devuelve el IAM (habilitan autorización por permiso).
  - Spring Security stateless; rutas públicas: documentación OpenAPI y `actuator/health`.
- Consecuencias:
  - Añade latencia y una dependencia del IAM en cada request autenticado; si el IAM no responde, el request se trata como no autenticado.
  - Al obtener permisos efectivos en cada validación, la autorización administrativa puede basarse en permisos reales del IAM.
  - Permite detectar estado de cuenta (activo/verificado) en cada request.
  - La validación remota exige que el IAM esté disponible; en entornos sin IAM se simula/mockea en tests.