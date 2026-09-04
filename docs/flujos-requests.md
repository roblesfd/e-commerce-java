# Flujos de integración — E-Commerce + IAM

Guía de ejemplo (curl) para el desarrollador que despliegue el sistema: qué endpoint se
ejecuta, en qué orden, con qué cuerpo y headers. Los flujos de la **sección A** involucran
al **IAM** (identidad); los de **B–D** son de e-commerce y usan el token solo para autenticar
(el IAM participa implícitamente en cada request vía `TokenValidator` → `POST /auth/validar`).

## Convenciones

- `IAM` = Identity Platform · `ECOMMERCE` = e-commerce (Spring Boot, puerto 8080).
- `{token}` = access token JWT · `{refresh}` = refresh token.
- Cualquier request de e-commerce con `Authorization: Bearer {token}` dispara la validación
  remota contra el IAM; si el token no es válido → `401`.
- La identidad del usuario sale del token (`SecurityUtils.requiredUserId()`); el
  e-commerce **nunca** crea el usuario IAM y el `iam_user_id` no viene del body.

---

# A. Identidad (involucran el IAM)

## 1. Registro completo (frontend orquesta)

Objetivo: crear el usuario en el IAM, autenticarse y completar el perfil comercial.

```bash
# 1.1 IAM — registrar el usuario
curl -X POST https://iam/api/usuarios \
  -H 'Content-Type: application/json' \
  -d '{"username":"ana","email":"ana@example.com","password":"SuperSecret1"}'

# 1.2 IAM — login (obtener token)
curl -X POST https://iam/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"identificador":"ana","contrasena":"SuperSecret1"}'
# → 200 {"token":"{token}","refreshToken":"{refresh}","expiraEnSegundos":3600}

# 1.3 ECOMMERCE — crear el perfil de cliente (ligado al usuario IAM del token)
curl -X POST http://localhost:8080/api/v1/customers \
  -H 'Authorization: Bearer {token}' \
  -H 'Content-Type: application/json' \
  -d '{"firstName":"Ana","lastName":"García","email":"ana@example.com","phoneNumber":null}'
# → 201 CustomerResponse (customer.iam_user_id = usuarioId del token)

# 1.4 ECOMMERCE — consultar el perfil
curl http://localhost:8080/api/v1/customers/me -H 'Authorization: Bearer {token}'
# → 200 perfil del cliente
```

## 2. Login y acceso por propiedad

Objetivo: autenticarse y acceder **solo** a los recursos propios (carrito, wishlists, órdenes).

```bash
# 2.1 IAM — login
curl -X POST https://iam/api/auth/login -H 'Content-Type: application/json' \
  -d '{"identificador":"ana@example.com","contrasena":"SuperSecret1"}'
# → 200 token

# 2.2 ECOMMERCE — recursos propios (el carrito es por usuario, todo bajo /me)
curl http://localhost:8080/api/v1/carts/me -H 'Authorization: Bearer {token}'
curl http://localhost:8080/api/v1/wishlists -H 'Authorization: Bearer {token}'
curl http://localhost:8080/api/v1/orders/me -H 'Authorization: Bearer {token}'
# → 200 cada uno, solo datos del usuario
```

## 3. Refresh y Logout (frontend → IAM)

```bash
# Renovar el access token
curl -X POST https://iam/api/auth/refresh -H 'Content-Type: application/json' \
  -d '{"refreshToken":"{refresh}"}'

# Cerrar sesión (revoca el refresh token)
curl -X POST https://iam/api/auth/logout -H 'Content-Type: application/json' \
  -d '{"refreshToken":"{refresh}"}'
```

## 4. Sesiones y permisos (reutilizando el starter)

> **Nota**: estos endpoints (`/api/v1/me/sessions`, `/api/v1/me/permissions`, `/api/v1/me`)
> corresponden al plan de reutilización del starter de IAM (inyectando `SessionClient`,
> `PermissionClient` y `UserClient`) y **aún no están implementados** en el código.

```bash
# 4.1 ECOMMERCE — mis sesiones/dispositivos (SessionClient.listar)
curl http://localhost:8080/api/v1/me/sessions -H 'Authorization: Bearer {token}'

# 4.2 ECOMMERCE — revocar una sesión (SessionClient.revocar)
curl -X DELETE http://localhost:8080/api/v1/me/sessions/{tokenSesion} \
  -H 'Authorization: Bearer {token}'
# → 204

# 4.3 ECOMMERCE — permisos efectivos en runtime (PermissionClient)
curl http://localhost:8080/api/v1/me/permissions -H 'Authorization: Bearer {token}'
```

## 5. Admin (rol + grupo)

Objetivo: operaciones administrativas exigiendo `ROLE_ADMINISTRADOR` **y** `GRUPO_ADMINISTRADORES`.

```bash
# 5.1 IAM — login de un admin
curl -X POST https://iam/api/auth/login -H 'Content-Type: application/json' \
  -d '{"identificador":"admin","contrasena":"AdminPass1"}'
# → 200 token (con permisos/roles/grupos del admin)

# 5.2 ECOMMERCE — eliminar producto (403 si falta rol o grupo)
curl -X DELETE http://localhost:8080/api/v1/admin/products/{productId} \
  -H 'Authorization: Bearer {tokenAdmin}'
# → 204 (y se registra en audit_log)

# 5.3 ECOMMERCE — consultar auditoría
curl http://localhost:8080/api/v1/admin/audit -H 'Authorization: Bearer {tokenAdmin}'
# → 200 [{"action":"DELETE_PRODUCT","actorId":"{usuarioId}",...}]
```

---

# B. Ciclo de compra (e-commerce; el IAM solo autentica)

## 6. Carrito

```bash
# 6.1 Crear carrito
curl -X POST http://localhost:8080/api/v1/carts -H 'Authorization: Bearer {token}'
# → 201 CartResponse

# 6.2 Añadir item
curl -X POST http://localhost:8080/api/v1/carts/me/items \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' \
  -d '{"productId":"{productId}","variantId":null,"quantity":2}'

# 6.3 Aplicar cupón (opcional)
curl -X POST http://localhost:8080/api/v1/carts/me/coupons \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' \
  -d '{"code":"BIENVENIDO"}'

# 6.4 Ver carrito
curl http://localhost:8080/api/v1/carts/me -H 'Authorization: Bearer {token}'
```

## 7. Checkout (reserva stock, cupón, orden y pago)

```bash
# 7.1 Iniciar checkout (valida productos/precios, reserva inventario, aplica cupón)
curl -X POST http://localhost:8080/api/v1/checkouts \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' \
  -d '{"addressId":"{addressId}"}'
# → 201 CheckoutResponse {id, status: PENDING, ...}

# 7.2 Confirmar (crea la orden, inicia el pago; aprueba → orden CONFIRMED / rechaza → cancel)
curl -X POST http://localhost:8080/api/v1/checkouts/{id}/confirm \
  -H 'Authorization: Bearer {token}'
# → 200 estado final (CONFIRMED o FAILED)
```

## 8. Webhook de pago (proveedor → e-commerce, **sin token**)

```bash
curl -X POST http://localhost:8080/api/v1/payments/webhooks \
  -H 'Content-Type: application/json' \
  -H 'X-Webhook-Signature: {firmaSecreta}' \
  -d '{"transactionId":"{txn}","status":"captured"}'
# → 200 (idempotente; callbacks duplicados no re-capturan)
```

## 9. Envío (orden → shipping → entrega)

```bash
# 9.1 Crear envío (requiere orden CONFIRMED)
curl -X POST http://localhost:8080/api/v1/shipments \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' \
  -d '{"orderId":"{orderId}","shippingMethodId":"{methodId}","addressId":"{addressId}"}'
# → 201 {status: PENDING, trackingNumber}

# 9.2 Avanzar estados (sistema/operador)
for s in prepared shipped in-transit out-for-delivery delivered; do
  curl -X POST http://localhost:8080/api/v1/shipments/{id}/$s -H 'Authorization: Bearer {token}'
done

# 9.3 Webhook del proveedor (alternativa, sin token)
curl -X POST http://localhost:8080/api/v1/shipments/webhooks \
  -H 'Content-Type: application/json' -H 'X-Webhook-Signature: {firma}' \
  -d '{"trackingNumber":"{tracking}","status":"delivered"}'
# → 200 (al entregar también marca la orden DELIVERED)
```

---

# C. Postventa (e-commerce)

## 10. Devolución y reembolso

```bash
# 10.1 Crear devolución (orden DELIVERED, dentro de la ventana, cantidad ≤ comprada)
curl -X POST http://localhost:8080/api/v1/returns \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' \
  -d '{"orderId":"{orderId}","reason":"DEFECTIVE","items":[{"orderItemId":"{itemId}","quantity":1}]}'
# → 201 {status: REQUESTED}

# 10.2 Ciclo de devolución
curl -X POST http://localhost:8080/api/v1/returns/{id}/approve -H 'Authorization: Bearer {token}'
curl -X POST http://localhost:8080/api/v1/returns/{id}/return-shipment \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' -d '{"trackingNumber":"RTN-1"}'
curl -X POST http://localhost:8080/api/v1/returns/{id}/receive -H 'Authorization: Bearer {token}'
curl -X POST http://localhost:8080/api/v1/returns/{id}/inspect \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' \
  -d '{"condition":"BUENO","notes":null,"passed":true}'
curl -X POST http://localhost:8080/api/v1/returns/{id}/complete -H 'Authorization: Bearer {token}'
# → 200 {status: COMPLETED, refundStatus: REFUNDED} (solicita reembolso al contexto Payment)
```

## 11. Reviews y moderación

```bash
# 11.1 Crear review (verifica compra DELIVERED → verifiedPurchase)
curl -X POST http://localhost:8080/api/v1/reviews \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' \
  -d '{"productId":"{productId}","rating":5,"title":"Excelente","comment":"Muy bueno"}'
# → 201 {status: PENDING_MODERATION, verifiedPurchase: true}

# 11.2 Moderar (solo admin con rol/grupo)
curl -X POST http://localhost:8080/api/v1/admin/reviews/{id}/approve \
  -H 'Authorization: Bearer {tokenAdmin}'
# → 200 {status: PUBLISHED}
```

---

# D. Servicios (e-commerce)

## 12. Wishlist

```bash
# 12.1 Crear wishlist
curl -X POST http://localhost:8080/api/v1/wishlists \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' -d '{"name":"Favoritos"}'

# 12.2 Añadir item (valida producto activo y, si es variante, stock > 0)
curl -X POST http://localhost:8080/api/v1/wishlists/{id}/items \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' \
  -d '{"productId":"{productId}","variantId":null}'
# → 201 (422 si el producto es inactivo o la variante no tiene stock)
```

## 13. Notificaciones

```bash
# 13.1 Crear y entregar (IN_APP → SENT directo; externos → proveedor mock con Retry)
curl -X POST http://localhost:8080/api/v1/notifications \
  -H 'Authorization: Bearer {token}' -H 'Content-Type: application/json' \
  -d '{"customerId":"{customerId}","channel":"IN_APP","title":"Pedido enviado","body":"En camino"}'
# → 201 {status: SENT}

# 13.2 Reintentar una fallida (solo FAILED)
curl -X POST http://localhost:8080/api/v1/notifications/{id}/retry -H 'Authorization: Bearer {token}'
```

## 14. Búsqueda e índice

```bash
# 14.1 Indexar un producto (admin)
curl -X POST http://localhost:8080/api/v1/search/index/products/{productId} \
  -H 'Authorization: Bearer {tokenAdmin}'
# → 200 {indexStatus: SYNCED} (FAILED si algo falla; reintentar con .../retry)

# 14.2 Rebuild completo
curl -X POST http://localhost:8080/api/v1/search/index/rebuild -H 'Authorization: Bearer {tokenAdmin}'

# 14.3 Buscar (con token; texto, filtros, orden, paginación)
curl 'http://localhost:8080/api/v1/search/products?q=camisa&available=true&sort=PRICE_ASC&page=0&size=20' \
  -H 'Authorization: Bearer {token}'
# → 200 SearchResultsResponse
```