# Diagramas de secuencia — E-Commerce

Flujos representativos entre contextos (dependencias solo por puertos).

## 1. Checkout (dos fases: start + confirm)

```mermaid
sequenceDiagram
    participant C as Cliente
    participant CH as CheckoutService
    participant CAT as Catalog
    participant PR as Pricing
    participant PRO as Promotion
    participant INV as Inventory
    participant ORD as Order
    participant PAY as Payment

    C->>CH: POST /api/v1/checkout (items, cupón, dirección, idempotencyKey)
    loop por cada item
        CH->>CAT: ProductProvider — productInfoOf (validar producto)
        CAT-->>CH: producto
        CH->>PR: PriceProvider — currentPrice
        PR-->>CH: precio
        CH->>INV: InventoryReservationPort — reserve (TTL)
        INV-->>CH: reserva ACTIVE
    end
    CH->>PRO: CouponProvider — discountFor (si hay cupón)
    PRO-->>CH: descuento
    CH->>CH: Checkout.start (PENDING, idempotente por key)
    CH-->>C: 201 — checkoutId

    C->>CH: POST /checkout/{id}/confirm
    CH->>ORD: OrderServicePort — createOrder (PENDING)
    ORD-->>CH: orderId
    CH->>PAY: PaymentContextPaymentPort — initiate
    PAY-->>CH: resultado (approved?)
    alt Pago aprobado
        CH->>ORD: confirm(orderId)
        ORD-->>CH: orden CONFIRMED
        CH->>CH: checkout CONFIRMED
    else Pago rechazado
        CH->>ORD: cancel(orderId)
        CH->>INV: releaseAll(reservas)
        INV-->>CH: stock devuelto
        CH->>CH: checkout FAILED
    end
    CH-->>C: 200 — estado final
```

## 2. Fulfillment y envío (orden → shipping → entrega)

```mermaid
sequenceDiagram
    participant S as Sistema/Operador
    participant SHIP as ShipmentService
    participant ORD as OrderService
    participant PROV as ShippingProvider

    S->>ORD: confirm → process → markReadyToShip
    S->>SHIP: crearEnvío (orden CONFIRMED)
    SHIP->>PROV: createShipment (carrier, dirección)
    PROV-->>SHIP: trackingNumber
    SHIP-->>S: envío PENDING
    S->>SHIP: markPrepared
    S->>SHIP: markShipped
    SHIP->>ORD: OrderPort — markShipped (envío SHIPPED)
    ORD-->>SHIP: orden SHIPPED
    S->>SHIP: markInTransit → markOutForDelivery
    S->>SHIP: markDelivered
    SHIP->>ORD: OrderPort — markDelivered
    ORD-->>SHIP: orden DELIVERED
    SHIP-->>S: envío DELIVERED
```

## 3. Devolución y reembolso (returns → payment)

```mermaid
sequenceDiagram
    participant O as Operador
    participant RT as ReturnService
    participant ORD as Order
    participant PAY as Payment

    O->>RT: crearDevolución (orderId, items)
    RT->>ORD: OrderPort — getOrder (validar DELIVERED + ventana)
    ORD-->>RT: orden DELIVERED
    RT-->>O: devolución REQUESTED
    O->>RT: approve (calcula refundAmount)
    RT-->>O: APPROVED
    O->>RT: return-shipment → receive → inspect
    O->>RT: complete
    RT->>PAY: PaymentPort — refund(orderId, monto)
    PAY-->>RT: reembolso aplicado
    RT-->>O: COMPLETED — refundStatus REFUNDED
```

## 4. Indexación de búsqueda (search index)

```mermaid
sequenceDiagram
    participant A as Admin
    participant IX as IndexService
    participant CAT as Catalog
    participant PR as Pricing
    participant INV as Inventory
    participant IDX as SearchDocumentRepository

    A->>IX: indexProduct(productId)
    IX->>CAT: CatalogPort — findProduct
    CAT-->>IX: producto
    IX->>PR: PricingPort — currentPrice
    PR-->>IX: precio
    IX->>INV: InventoryPort — availableStock
    INV-->>IX: stock
    IX->>IDX: save(search_document)
    IDX-->>IX: documento SYNCED
    IX-->>A: indexStatus SYNCED
    Note over IX,IDX: ante fallo → documento FAILED + reintento (retry)
```

## 5. Operación administrativa con auditoría

```mermaid
sequenceDiagram
    participant A as Admin
    participant ACS as AdminCatalogService
    participant AUD as AuditService
    participant PROD as ProductUseCases

    A->>ACS: DELETE /api/v1/admin/products/{id} [@RequiresAdmin]
    ACS->>AUD: record(DELETE_PRODUCT, actor, productId)
    AUD-->>ACS: audit_log guardado
    ACS->>PROD: products.delete(productId)
    PROD-->>ACS: ok
    ACS-->>A: 204 No Content
```
## 6. Webhook de pago (proveedor → payment)

```mermaid
sequenceDiagram
    participant P as Proveedor de pagos
    participant WH as PaymentWebhookController
    participant PS as PaymentService
    participant PAY as Payment

    P->>WH: POST /api/v1/payments/webhooks (transactionId, status, firma)
    WH->>WH: validar X-Webhook-Signature (secreto compartido)
    WH->>PS: processWebhook(transactionId, providerStatus)
    PS->>PAY: aplicar transición (CAPTURED / REFUNDED)
    PAY-->>PS: pago actualizado (idempotente)
    PS-->>WH: pago
    WH-->>P: 200 OK
    Note over PS,PAY: callbacks duplicados/tardíos no re-capturan ni re-reembolsan
```

## 7. Webhook de envío (proveedor → shipping → order)

```mermaid
sequenceDiagram
    participant P as Proveedor de envíos
    participant WH as ShipmentWebhookController
    participant SS as ShipmentService
    participant ORD as OrderService

    P->>WH: POST /api/v1/shipments/webhooks (tracking, status, firma)
    WH->>WH: validar firma
    WH->>SS: processWebhook(trackingNumber, status)
    SS->>SS: avanzar estado del envío (IN_TRANSIT → DELIVERED)
    SS->>ORD: OrderPort — markDelivered
    ORD-->>SS: orden DELIVERED
    SS-->>WH: envío DELIVERED
    WH-->>P: 200 OK
```

## 8. Ciclo de reservas de inventario

```mermaid
sequenceDiagram
    participant CH as CheckoutService
    participant INV as InventoryService
    participant RES as stock_reservation

    CH->>INV: reserveStock(producto, variante, cantidad)
    INV->>RES: reserva ACTIVE (retiene on_hand - reserved)
    RES-->>INV: reserva creada
    INV-->>CH: StockReservation
    alt Confirmación (orden creada)
        CH->>INV: confirmReservation(id)
        INV->>RES: CONFIRMED (stock descontado)
    else Cancelación
        CH->>INV: releaseReservation(id)
        INV->>RES: RELEASED (stock devuelto)
    else Vencimiento
        INV->>INV: expireReservation(id)
        INV->>RES: EXPIRED (stock devuelto)
    end
```

## 9. Creación de review y moderación

```mermaid
sequenceDiagram
    participant C as Cliente
    participant RS as ReviewService
    participant ORD as Order
    participant REPO as ReviewRepository
    participant A as Admin

    C->>RS: createReview(productId, rating, comentario)
    RS->>ORD: OrderPort — verifyPurchase(customerId, productId)
    ORD-->>RS: orderItem (si orden DELIVERED)
    RS->>REPO: save(review PENDING_MODERATION, verifiedPurchase)
    REPO-->>RS: review
    RS-->>C: 201 PENDING_MODERATION
    A->>RS: approve(reviewId)
    RS->>REPO: save(review PUBLISHED)
    REPO-->>RS: review publicado
    RS-->>A: PUBLISHED
```

## 10. Wishlist: añadir item con validación

```mermaid
sequenceDiagram
    participant C as Cliente
    participant WS as WishlistService
    participant CAT as Catalog
    participant INV as Inventory
    participant WR as WishlistRepository

    C->>WS: addItem(wishlistId, productId, variantId?)
    WS->>CAT: CatalogPort — findProduct (activo, no borrado)
    CAT-->>WS: producto
    alt Con variante
        WS->>CAT: validar variante existe
        WS->>INV: InventoryPort — availableStock
        INV-->>WS: stock
    end
    WS->>WR: save(wishlist + item)
    WR-->>WS: wishlist actualizada
    WS-->>C: 201 item añadido
    Note over WS,WR: producto inactivo/variante sin stock → 422
```

## 11. Notificación: entrega y reintento

```mermaid
sequenceDiagram
    participant S as Sistema
    participant NS as NotificationService
    participant PROV as NotificationProvider
    participant NR as NotificationRepository

    S->>NS: createNotification(customerId, canal, título, cuerpo)
    alt Canal IN_APP
        NS->>NR: save(PENDING → SENT, no leída)
    else Canal externo (EMAIL/SMS/PUSH)
        NS->>PROV: send(canal, customerId, título, cuerpo) [Retry]
        PROV-->>NS: DeliveryResult
        alt Éxito
            NS->>NR: save(SENT)
        else Fallo
            NS->>NR: save(FAILED)
        end
    end
    S->>NS: retry(notificationId) [solo FAILED]
    NS->>PROV: send(...)
    NS->>NR: save(SENT)
```

## 12. Búsqueda de productos

```mermaid
sequenceDiagram
    participant C as Cliente
    participant SC as SearchController
    participant SS as SearchService
    participant IDX as SearchDocumentRepository

    C->>SC: GET /api/v1/search/products (q, filtros, sort, page, size)
    SC->>SS: search(SearchQuery)
    SS->>IDX: search (Specification + Pageable)
    IDX->>IDX: ILIKE trigram + filtros (categoría/brand/precio/disponibilidad/atributos)
    IDX-->>SS: SearchResults (página)
    SS-->>SC: resultados
    SC-->>C: SearchResultsResponse
```
