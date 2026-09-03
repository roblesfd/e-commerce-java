-- Contexto Search: índice de búsqueda denormalizado del catálogo.

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE search_document (
    product_id     UUID         PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    TEXT,
    sku            VARCHAR(64),
    barcode        VARCHAR(64),
    attributes     TEXT,
    brand_id       UUID,
    price          NUMERIC(19,2),
    currency       VARCHAR(3),
    available      BOOLEAN      NOT NULL DEFAULT FALSE,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    searchable_text TEXT        NOT NULL,
    index_status   VARCHAR(10)  NOT NULL DEFAULT 'SYNCED',
    last_error     VARCHAR(500),
    attempts       INT          NOT NULL DEFAULT 0,
    updated_at     TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE search_document_category (
    product_id  UUID NOT NULL REFERENCES search_document (product_id) ON DELETE CASCADE,
    category_id UUID NOT NULL
);

CREATE INDEX idx_search_document_fts ON search_document USING GIN (to_tsvector('simple', searchable_text));
CREATE INDEX idx_search_document_name_trgm ON search_document USING GIN (name gin_trgm_ops);
CREATE INDEX idx_search_document_text_trgm ON search_document USING GIN (searchable_text gin_trgm_ops);
CREATE INDEX idx_search_document_category ON search_document_category (category_id);
CREATE INDEX idx_search_document_brand ON search_document (brand_id);
CREATE INDEX idx_search_document_price ON search_document (price);