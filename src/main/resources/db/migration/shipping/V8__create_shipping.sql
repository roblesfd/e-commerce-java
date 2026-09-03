-- Contexto Shipping: métodos de envío y envíos asociados a órdenes.

CREATE TABLE shipping_method (
    id          UUID         PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    carrier     VARCHAR(120) NOT NULL,
    cost_amount NUMERIC(19,2) NOT NULL,
    currency    VARCHAR(3)   NOT NULL,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT ck_shipping_method_cost CHECK (cost_amount >= 0)
);

CREATE TABLE shipment (
    id                 UUID         PRIMARY KEY,
    order_id           UUID         NOT NULL UNIQUE,
    customer_id        UUID         NOT NULL,
    shipping_method_id UUID         NOT NULL REFERENCES shipping_method (id),
    address_id         UUID,
    address_snapshot   VARCHAR(500),
    tracking_number    VARCHAR(128),
    status             VARCHAR(20)  NOT NULL,
    cost_amount        NUMERIC(19,2) NOT NULL,
    currency           VARCHAR(3)   NOT NULL,
    created_at         TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_shipment_customer ON shipment (customer_id);

CREATE TABLE shipment_status_history (
    shipment_id UUID        NOT NULL REFERENCES shipment (id) ON DELETE CASCADE,
    status      VARCHAR(20) NOT NULL,
    at          TIMESTAMP   NOT NULL
);

CREATE INDEX idx_shipment_history_shipment ON shipment_status_history (shipment_id);