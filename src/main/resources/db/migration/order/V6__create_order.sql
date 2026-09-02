-- Contexto Order Management: orden y sus items con snapshots históricos
-- (producto, precio, descuento) y el historial de estados.

CREATE TABLE orders (
    id               UUID         PRIMARY KEY,
    customer_id      UUID         NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    address_id       UUID,
    address_snapshot VARCHAR(500),
    coupon_code      VARCHAR(64),
    subtotal_amount  NUMERIC(19,2) NOT NULL,
    discounts_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_amount     NUMERIC(19,2) NOT NULL,
    currency         VARCHAR(3)   NOT NULL,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_orders_customer ON orders (customer_id);

CREATE TABLE order_item (
    id             UUID         PRIMARY KEY,
    order_id       UUID         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id     UUID         NOT NULL,
    variant_id     UUID,
    sku            VARCHAR(64),
    name           VARCHAR(200),
    quantity       INT          NOT NULL,
    unit_amount    NUMERIC(19,2) NOT NULL,
    unit_currency  VARCHAR(3)   NOT NULL,
    subtotal_amount NUMERIC(19,2) NOT NULL,
    position       INT          NOT NULL
);

CREATE INDEX idx_order_item_order ON order_item (order_id);

CREATE TABLE order_status_history (
    order_id UUID        NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    status   VARCHAR(20) NOT NULL,
    at       TIMESTAMP   NOT NULL
);

CREATE INDEX idx_order_history_order ON order_status_history (order_id);