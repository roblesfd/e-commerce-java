-- Contexto Reviews: reviews y ratings de productos.

CREATE TABLE review (
    id               UUID         PRIMARY KEY,
    customer_id      UUID         NOT NULL,
    product_id       UUID         NOT NULL,
    variant_id       UUID,
    order_id         UUID,
    order_item_id    UUID,
    rating           INT          NOT NULL,
    title            VARCHAR(150),
    comment          VARCHAR(2000) NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    verified_purchase BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at       TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT ck_review_rating CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX idx_review_product ON review (product_id);
CREATE INDEX idx_review_customer ON review (customer_id);

CREATE TABLE review_status_history (
    review_id UUID        NOT NULL REFERENCES review (id) ON DELETE CASCADE,
    status    VARCHAR(20) NOT NULL,
    at        TIMESTAMP   NOT NULL
);

CREATE INDEX idx_review_history_review ON review_status_history (review_id);

CREATE TABLE review_reply (
    review_id UUID         NOT NULL REFERENCES review (id) ON DELETE CASCADE,
    reply_id  UUID         NOT NULL,
    author_id UUID         NOT NULL,
    comment   VARCHAR(1000) NOT NULL,
    at        TIMESTAMP    NOT NULL
);

CREATE INDEX idx_review_reply_review ON review_reply (review_id);