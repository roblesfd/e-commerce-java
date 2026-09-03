package org.fernandodev.ecommerce.search.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import org.junit.jupiter.api.Test;

class SearchDocumentTest {

    private static final Instant T1 = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createCreaDocumentoSyncronizado() {
        SearchDocument document = documento();

        assertThat(document.getIndexStatus()).isEqualTo(IndexStatus.SYNCED);
        assertThat(document.getAttempts()).isZero();
        assertThat(document.isAvailable()).isTrue();
    }

    @Test
    void markFailedAcumulaIntentosYError() {
        SearchDocument document = documento();

        document.markFailed("timeout", T1);
        document.markFailed("timeout", T1);

        assertThat(document.getIndexStatus()).isEqualTo(IndexStatus.FAILED);
        assertThat(document.getLastError()).isEqualTo("timeout");
        assertThat(document.getAttempts()).isEqualTo(2);
    }

    @Test
    void markSyncedLimpiaElError() {
        SearchDocument document = documento();
        document.markFailed("timeout", T1);

        document.markSynced(T1);

        assertThat(document.getIndexStatus()).isEqualTo(IndexStatus.SYNCED);
        assertThat(document.getLastError()).isNull();
    }

    private static SearchDocument documento() {
        return SearchDocument.create(ProductId.generate(), "Camisa", "Camisa de algodón", "SKU-1", null,
                "color:blanco", Set.of(CategoryId.generate()), UUID.randomUUID(), Money.of("900", "USD"),
                true, true, "camisa camisa de algodón sku-1 color:blanco", T1);
    }
}