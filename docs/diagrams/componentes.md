# Diagrama de componentes — E-Commerce (monolito modular hexagonal)

Vista de alto nivel de los bounded contexts y sus dependencias (solo por puertos de aplicación).

```mermaid
flowchart LR
  subgraph EXTERNO["Externo"]
    IAM["IAM — validación de tokens"]
  end

  subgraph BASE["shared — infraestructura / seguridad"]
    SHARED["shared (security, errors, flyway)"]
  end

  subgraph DOMINIO["Bounded contexts (propietarios de datos)"]
    CUSTOMER["customer"]
    CATALOG["catalog"]
    PRICING["pricing"]
    INVENTORY["inventory"]
    PROMOTION["promotion"]
    ORDER["order"]
    PAYMENT["payment"]
    SHIPPING["shipping"]
    RETURNS["returns"]
    REVIEWS["reviews"]
    WISHLIST["wishlist"]
    NOTIFICATIONS["notifications"]
    SEARCH["search"]
  end

  subgraph ORQ["Orquestación / entradas"]
    CART["cart (Redis)"]
    CHECKOUT["checkout"]
    ADMIN["admin"]
  end

  subgraph INFRA["Infraestructura"]
    PG[("PostgreSQL")]
    REDIS[("Redis")]
  end

  IAM -. "JwtAuthenticationFilter" .-> SHARED

  CART --> REDIS
  SHARED --> IAM

  CHECKOUT -->|"CatalogProductProvider"| CATALOG
  CHECKOUT -->|"PriceProvider"| PRICING
  CHECKOUT -->|"CouponProvider"| PROMOTION
  CHECKOUT -->|"OrderPort"| ORDER
  CHECKOUT -->|"PaymentPort"| PAYMENT
  CHECKOUT -->|"InventoryReservationPort"| INVENTORY

  SHIPPING -->|"OrderPort"| ORDER
  SHIPPING -->|"CustomerAddressProvider"| CUSTOMER

  RETURNS -->|"OrderPort"| ORDER
  RETURNS -->|"PaymentPort"| PAYMENT

  REVIEWS -->|"OrderPort"| ORDER

  SEARCH -->|"CatalogPort"| CATALOG
  SEARCH -->|"PricingPort"| PRICING
  SEARCH -->|"InventoryPort"| INVENTORY

  WISHLIST -->|"CatalogPort"| CATALOG
  WISHLIST -->|"InventoryPort"| INVENTORY

  ADMIN --> CUSTOMER
  ADMIN --> CATALOG
  ADMIN --> PRICING
  ADMIN --> PROMOTION
  ADMIN --> INVENTORY
  ADMIN --> ORDER
  ADMIN --> PAYMENT
  ADMIN --> RETURNS
  ADMIN --> REVIEWS
  ADMIN --> SEARCH

  CUSTOMER --> PG
  CATALOG --> PG
  PRICING --> PG
  INVENTORY --> PG
  PROMOTION --> PG
  ORDER --> PG
  PAYMENT --> PG
  SHIPPING --> PG
  RETURNS --> PG
  REVIEWS --> PG
  WISHLIST --> PG
  NOTIFICATIONS --> PG
  SEARCH --> PG
  ADMIN --> PG
```

## Notas

- **Dependencias solo por puertos** (`application/port/in` de otros contextos): nunca se accede al repositorio o dominio de otro contexto.
- `cart` y `checkout` no poseen tablas: el carrito vive en Redis y checkout es capa de orquestación.
- `order` no consume puertos de otros contextos: captura los precios/snapshots en el comando de creación.
- `shared` incluye Spring Security (filtro JWT con validación remota contra IAM), manejador global de errores y las migraciones Flyway comunes.