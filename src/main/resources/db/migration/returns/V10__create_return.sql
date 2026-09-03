-- Contexto Returns & Refunds: políticas de devolución, solicitudes de retorno
-- y su ciclo de vida.

CREATE TABLE return_policy (
    id                 UUID        PRIMARY KEY,
    return_window_days INT         NOT NULL,
    active             BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT ck_return_policy_days CHECK (return_window_days > 0)
);

CREATE TABLE return_policy_product (
    return_policy_id UUID NOT NULL REFERENCES return_policy (id) ON DELETE CASCADE,
    product_id      UUID NOT NULL,
    PRIMARY KEY (return_policy_id, product_id)
);

CREATE TABLE return_request (
    id              UUID         PRIMARY KEY,
    order_id        UUID         NOT NULL,
    customer_id     UUID         NOT NULL,
    reason          VARCHAR(50)  NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    tracking_number VARCHAR(128),
    refund_amount   NUMERIC(19,2),
    refund_currency VARCHAR(3),
    refund_status   VARCHAR(20),
    refund_requested BOOLEAN     NOT NULL DEFAULT FALSE,
    inspection_condition VARCHAR(100),
    inspection_notes     VARCHAR(500),
    inspection_passed    BOOLEAN,
    inspection_at        TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_return_customer ON return_request (customer_id);
CREATE INDEX idx_return_order ON return_request (order_id);

CREATE TABLE return_item (
    return_id      UUID         NOT NULL REFERENCES return_request (id) ON DELETE CASCADE,
    order_item_id  UUID         NOT NULL,
    product_id     UUID         NOT NULL,
    quantity       INT          NOT NULL,
    refund_amount  NUMERIC(19,2) NOT NULL,
    currency       VARCHAR(3)   NOT NULL
);

CREATE INDEX idx_return_item_return ON return_item (return_id);

CREATE TABLE return_status_history (
    return_id UUID        NOT NULL REFERENCES return_request (id) ON DELETE CASCADE,
    status    VARCHAR(20) NOT NULL,
    at        TIMESTAMP   NOT NULL
);

CREATE INDEX idx_return_history_return ON return_status_history (return_id);