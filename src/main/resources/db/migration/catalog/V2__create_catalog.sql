-- Contexto Product Catalog: brand, category, product, variantes y relaciones.
-- Precio e inventario pertenecen a sus propios bounded contexts (pricing/inventory),
-- por lo que product y product_variant no exponen columnas de precio ni stock.

CREATE TABLE brand (
    id         UUID         PRIMARY KEY,
    name       VARCHAR(120) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP
);

CREATE TABLE category (
    id         UUID         PRIMARY KEY,
    parent_id  UUID         REFERENCES category (id),
    name       VARCHAR(120) NOT NULL,
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at TIMESTAMP    NOT NULL DEFAULT now(),
    deleted_at TIMESTAMP
);

CREATE INDEX idx_category_parent ON category (parent_id);

CREATE TABLE product (
    id          UUID         PRIMARY KEY,
    sku         VARCHAR(64)  NOT NULL UNIQUE,
    barcode     VARCHAR(64)  UNIQUE,
    name        VARCHAR(200) NOT NULL,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    brand_id    UUID         REFERENCES brand (id),
    created_at  TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMP
);

CREATE INDEX idx_product_brand ON product (brand_id);
CREATE INDEX idx_product_active ON product (active);

CREATE TABLE product_variant (
    id         UUID        PRIMARY KEY,
    product_id UUID        NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    sku        VARCHAR(64) UNIQUE,
    barcode    VARCHAR(64) UNIQUE,
    position   INT         NOT NULL
);

CREATE INDEX idx_variant_product ON product_variant (product_id);

CREATE TABLE product_variant_attribute (
    variant_id UUID         NOT NULL REFERENCES product_variant (id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    value      VARCHAR(500) NOT NULL,
    PRIMARY KEY (variant_id, name)
);

CREATE TABLE product_attribute (
    product_id UUID         NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    value      VARCHAR(500) NOT NULL,
    PRIMARY KEY (product_id, name)
);

CREATE TABLE product_image (
    product_id UUID         NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    position   INT          NOT NULL,
    PRIMARY KEY (product_id, position)
);

CREATE TABLE product_category (
    product_id  UUID NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    category_id UUID NOT NULL REFERENCES category (id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, category_id)
);

CREATE INDEX idx_product_category_category ON product_category (category_id);