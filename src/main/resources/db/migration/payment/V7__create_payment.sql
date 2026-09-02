-- Contexto Payments: pago asociado a una orden y su historial de eventos.
-- order_id es único (un pago por orden) y request_id garantiza idempotencia.

CREATE TABLE payment (
    id                      UUID          PRIMARY KEY,
    order_id                UUID          NOT NULL UNIQUE,
    request_id              VARCHAR(64)   NOT NULL UNIQUE,
    amount                  NUMERIC(19,2) NOT NULL,
    currency                VARCHAR(3)    NOT NULL,
    status                  VARCHAR(20)   NOT NULL,
    provider_transaction_id VARCHAR(128),
    created_at              TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT ck_payment_amount CHECK (amount > 0)
);

CREATE INDEX idx_payment_order ON payment (order_id);

CREATE TABLE payment_event (
    payment_id UUID         NOT NULL REFERENCES payment (id) ON DELETE CASCADE,
    type       VARCHAR(30)  NOT NULL,
    detail     VARCHAR(500),
    at         TIMESTAMP    NOT NULL
);

CREATE INDEX idx_payment_event_payment ON payment_event (payment_id);