-- Contexto Customer Management: agregado customer y sus direcciones.

CREATE TABLE customer (
    id                  UUID         PRIMARY KEY,
    iam_user_id         UUID         NOT NULL UNIQUE,
    first_name          VARCHAR(100) NOT NULL,
    last_name           VARCHAR(100) NOT NULL,
    email               VARCHAR(255) NOT NULL UNIQUE,
    phone_number        VARCHAR(30),
    default_address_id  UUID,
    newsletter_enabled  BOOLEAN      NOT NULL DEFAULT FALSE,
    preferred_currency  VARCHAR(3)   NOT NULL DEFAULT 'USD',
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE customer_address (
    customer_id UUID         NOT NULL REFERENCES customer (id) ON DELETE CASCADE,
    position    INT          NOT NULL,
    id          UUID         NOT NULL,
    street      VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    state       VARCHAR(100),
    postal_code VARCHAR(20)  NOT NULL,
    country     VARCHAR(100) NOT NULL,
    PRIMARY KEY (customer_id, position)
);
