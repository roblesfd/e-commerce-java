-- Contexto Wishlist: listas de deseos por cliente.

CREATE TABLE wishlist (
    id          UUID         PRIMARY KEY,
    customer_id UUID         NOT NULL,
    name        VARCHAR(100) NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_wishlist_customer ON wishlist (customer_id);

CREATE TABLE wishlist_item (
    wishlist_id UUID      NOT NULL REFERENCES wishlist (id) ON DELETE CASCADE,
    product_id  UUID      NOT NULL,
    variant_id  UUID,
    added_at    TIMESTAMP NOT NULL
);

CREATE INDEX idx_wishlist_item_wishlist ON wishlist_item (wishlist_id);