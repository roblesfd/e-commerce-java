-- Contexto Inventory: stock por producto/variante, reservas temporales y
-- movimientos. Sin FK a catalog para mantener el aislamiento entre bounded
-- contexts; product_id/variant_id son referencias.

CREATE TABLE inventory (
    id            UUID         PRIMARY KEY,
    product_id    UUID         NOT NULL,
    variant_id    UUID,
    on_hand       INT          NOT NULL,
    reserved      INT          NOT NULL DEFAULT 0,
    minimum_stock INT          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_inventory_target UNIQUE NULLS NOT DISTINCT (product_id, variant_id),
    CONSTRAINT ck_inventory_non_negative CHECK (on_hand >= 0 AND reserved >= 0 AND minimum_stock >= 0)
);

CREATE INDEX idx_inventory_product ON inventory (product_id);

CREATE TABLE stock_reservation (
    id          UUID        PRIMARY KEY,
    inventory_id UUID       NOT NULL REFERENCES inventory (id) ON DELETE CASCADE,
    quantity    INT         NOT NULL,
    status      VARCHAR(20) NOT NULL,
    expires_at  TIMESTAMP   NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT ck_reservation_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_reservation_inventory ON stock_reservation (inventory_id);

CREATE TABLE stock_movement (
    inventory_id UUID        NOT NULL REFERENCES inventory (id) ON DELETE CASCADE,
    type         VARCHAR(20) NOT NULL,
    quantity     INT         NOT NULL,
    reason       VARCHAR(200),
    at           TIMESTAMP   NOT NULL
);

CREATE INDEX idx_movement_inventory ON stock_movement (inventory_id);