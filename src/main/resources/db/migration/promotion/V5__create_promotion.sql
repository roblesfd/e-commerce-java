-- Contexto Promotions & Coupons: promociones (reglas de descuento), cupones y
-- uso por cliente. Sin FK a catalog para mantener el aislamiento entre bounded
-- contexts; product_id/category_id son referencias.

CREATE TABLE promotion (
    id                      UUID         PRIMARY KEY,
    name                    VARCHAR(120) NOT NULL,
    discount_type           VARCHAR(20)  NOT NULL,
    active                  BOOLEAN      NOT NULL DEFAULT TRUE,
    valid_from              TIMESTAMP    NOT NULL,
    valid_to                TIMESTAMP    NOT NULL,
    percentage_value        NUMERIC(5,2),
    fixed_amount            NUMERIC(19,2),
    fixed_currency          VARCHAR(3),
    buy_quantity            INT,
    get_quantity            INT,
    min_purchase_amount     NUMERIC(19,2),
    min_purchase_currency   VARCHAR(3),
    max_discount_amount     NUMERIC(19,2),
    max_discount_currency   VARCHAR(3),
    max_usage               INT          NOT NULL DEFAULT 0,
    max_usage_per_customer  INT          NOT NULL DEFAULT 0,
    used_count              INT          NOT NULL DEFAULT 0,
    created_at              TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT ck_promotion_validity CHECK (valid_from < valid_to),
    CONSTRAINT ck_promotion_usage CHECK (max_usage >= 0 AND max_usage_per_customer >= 0 AND used_count >= 0)
);

CREATE TABLE promotion_scope_product (
    promotion_id UUID NOT NULL REFERENCES promotion (id) ON DELETE CASCADE,
    product_id   UUID NOT NULL,
    PRIMARY KEY (promotion_id, product_id)
);

CREATE TABLE promotion_scope_category (
    promotion_id UUID NOT NULL REFERENCES promotion (id) ON DELETE CASCADE,
    category_id  UUID NOT NULL,
    PRIMARY KEY (promotion_id, category_id)
);

CREATE TABLE promotion_volume_tier (
    promotion_id  UUID         NOT NULL REFERENCES promotion (id) ON DELETE CASCADE,
    min_quantity  INT          NOT NULL,
    discount_type VARCHAR(20)  NOT NULL,
    value         NUMERIC(19,2) NOT NULL,
    PRIMARY KEY (promotion_id, min_quantity)
);

CREATE TABLE promotion_usage (
    promotion_id UUID NOT NULL REFERENCES promotion (id) ON DELETE CASCADE,
    customer_id  UUID NOT NULL,
    count        INT  NOT NULL DEFAULT 0,
    PRIMARY KEY (promotion_id, customer_id)
);

CREATE TABLE coupon (
    id           UUID         PRIMARY KEY,
    code         VARCHAR(64)  NOT NULL UNIQUE,
    promotion_id UUID         NOT NULL REFERENCES promotion (id),
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    valid_from   TIMESTAMP    NOT NULL,
    valid_to     TIMESTAMP    NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT now(),
    CONSTRAINT ck_coupon_validity CHECK (valid_from < valid_to)
);

CREATE INDEX idx_coupon_promotion ON coupon (promotion_id);