-- Contexto Pricing: precio base de producto/variante, historial de cambios
-- y overrides (promocional / por tiempo). Sin FK a catalog para mantener el
-- aislamiento entre bounded contexts; product_id/variant_id son referencias.

CREATE TABLE price (
    id          UUID         PRIMARY KEY,
    product_id  UUID         NOT NULL,
    variant_id  UUID,
    base_amount NUMERIC(19,2) NOT NULL,
    currency    VARCHAR(3)   NOT NULL,
    version     BIGINT       NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT uk_price_target UNIQUE NULLS NOT DISTINCT (product_id, variant_id)
);

CREATE INDEX idx_price_product ON price (product_id);

CREATE TABLE price_override (
    id         UUID         PRIMARY KEY,
    price_id   UUID         NOT NULL REFERENCES price (id) ON DELETE CASCADE,
    amount     NUMERIC(19,2) NOT NULL,
    currency   VARCHAR(3)   NOT NULL,
    valid_from TIMESTAMP    NOT NULL,
    valid_to   TIMESTAMP    NOT NULL,
    type       VARCHAR(20)  NOT NULL,
    CONSTRAINT ck_override_window CHECK (valid_from < valid_to)
);

CREATE INDEX idx_override_price ON price_override (price_id);

CREATE TABLE price_change (
    price_id     UUID         NOT NULL REFERENCES price (id) ON DELETE CASCADE,
    effective_at TIMESTAMP    NOT NULL,
    amount       NUMERIC(19,2) NOT NULL,
    currency     VARCHAR(3)   NOT NULL
);

CREATE INDEX idx_change_price ON price_change (price_id);