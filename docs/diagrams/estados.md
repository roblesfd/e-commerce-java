# Diagramas de estados — E-Commerce

Máquinas de estado de los agregados clave, extraídas del dominio (`requireTransition`).

## Order

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> CONFIRMED : confirm
    CONFIRMED --> PROCESSING : process
    PROCESSING --> READY_TO_SHIP : markReadyToShip
    READY_TO_SHIP --> SHIPPED : markShipped
    SHIPPED --> DELIVERED : markDelivered
    PENDING --> CANCELLED : cancel
    CONFIRMED --> CANCELLED : cancel
    DELIVERED --> [*]
    CANCELLED --> [*]
```

## Payment

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> AUTHORIZED : authorize
    AUTHORIZED --> CAPTURED : capture
    PENDING --> FAILED : reject / fallo proveedor
    PENDING --> CANCELLED : cancel
    AUTHORIZED --> CANCELLED : cancel
    CAPTURED --> REFUNDED : refund (completo)
    CAPTURED --> CAPTURED : refund parcial (acumula refundedAmount)
    REFUNDED --> [*]
```

## Shipment

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> PREPARING : markPrepared
    PREPARING --> SHIPPED : markShipped
    SHIPPED --> IN_TRANSIT : markInTransit
    IN_TRANSIT --> OUT_FOR_DELIVERY : markOutForDelivery
    OUT_FOR_DELIVERY --> DELIVERED : markDelivered
    PENDING --> CANCELLED : cancel
    PREPARING --> CANCELLED : cancel
    PENDING --> FAILED : handleFailure
    PREPARING --> FAILED : handleFailure
    SHIPPED --> FAILED : handleFailure
    IN_TRANSIT --> FAILED : handleFailure
    OUT_FOR_DELIVERY --> FAILED : handleFailure
    DELIVERED --> [*]
    FAILED --> [*]
    CANCELLED --> [*]
```

## Return

```mermaid
stateDiagram-v2
    [*] --> REQUESTED
    REQUESTED --> APPROVED : approve (calcula refundAmount)
    REQUESTED --> REJECTED : reject
    REQUESTED --> CANCELLED : cancel
    APPROVED --> CANCELLED : cancel
    APPROVED --> IN_TRANSIT : registerReturnShipment
    IN_TRANSIT --> RECEIVED : receive
    RECEIVED --> INSPECTED : inspect
    INSPECTED --> COMPLETED : complete (solicita refund a Payment)
```

## Review

```mermaid
stateDiagram-v2
    [*] --> PENDING_MODERATION
    PENDING_MODERATION --> PUBLISHED : approve
    PENDING_MODERATION --> REJECTED : reject
    PENDING_MODERATION --> DELETED : delete
    PUBLISHED --> HIDDEN : hide
    PUBLISHED --> DELETED : delete
    PUBLISHED --> PENDING_MODERATION : update (re-modera)
    HIDDEN --> PUBLISHED : restore
    HIDDEN --> DELETED : delete
```

## Notification

```mermaid
stateDiagram-v2
    [*] --> PENDING
    PENDING --> PROCESSING : entrega (markProcessing)
    PROCESSING --> SENT : proveedor ok (IN_APP inmediato)
    PROCESSING --> FAILED : proveedor falla / timeout
    FAILED --> PENDING : retry
    PENDING --> CANCELLED : cancel
```

## Stock reservation

```mermaid
stateDiagram-v2
    [*] --> ACTIVE
    ACTIVE --> CONFIRMED : confirmReservation (stock descontado)
    ACTIVE --> RELEASED : releaseReservation (stock devuelto)
    ACTIVE --> EXPIRED : expireReservation (stock devuelto)
```