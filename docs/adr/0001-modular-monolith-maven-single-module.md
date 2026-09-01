# ADR-0001: Modular monolith con un único módulo Maven

- Estado: Aceptado
- Fecha: 2026-09-01
- Contexto: el spec exige un Modular Monolith con arquitectura hexagonal y DDD, dividido en bounded contexts (customer, catalog, pricing, inventory, cart, promotion, checkout, order, payment), favoreciendo la simplicidad cuando la complejidad no está justificada.
- Decisión: se usa **un único módulo Maven** con los bounded contexts como paquetes Java independientes (`org.fernandodev.ecommerce.{contexto}/{domain,application,infrastructure,adapter}`), cada uno siguiendo hexagonal. No se usa multi-módulo Maven.
- Consecuencias:
  - El build es simple y reproducible; un solo artefacto ejecutable (fat jar).
  - El aislamiento es por convención de paquete y dependencias, no por compilación.
  - Si en el futuro se extrae un contexto a un servicio/módulo independiente, la separación en paquetes facilita la extracción.
  - El código compartido se minimiza (paquete `shared`).
