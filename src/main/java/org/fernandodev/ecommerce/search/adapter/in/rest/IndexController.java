package org.fernandodev.ecommerce.search.adapter.in.rest;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.adapter.in.rest.dto.IndexResponse;
import org.fernandodev.ecommerce.search.adapter.in.rest.mapper.SearchMapper;
import org.fernandodev.ecommerce.search.application.port.in.IndexUseCases;
import org.fernandodev.ecommerce.search.domain.SearchDocument;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST de mantenimiento del índice de búsqueda.
 */
@RestController
@RequestMapping("/api/v1/search/index")
@RequiredArgsConstructor
public class IndexController {

    private final IndexUseCases indexUseCases;
    private final SearchMapper mapper;

    @PostMapping("/products/{productId}")
    public IndexResponse index(@PathVariable UUID productId) {
        SearchDocument document = indexUseCases.indexProduct(ProductId.of(productId));
        if (document == null) {
            return new IndexResponse(productId, null, "Eliminado del índice", 0);
        }
        return mapper.toIndexResponse(document);
    }

    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Remove.
     * @param productId productId.
     */
    public void remove(@PathVariable UUID productId) {
        indexUseCases.removeProduct(ProductId.of(productId));
    }

    @PostMapping("/rebuild")
    /**
     * Rebuild.
     */
    public void rebuild() {
        indexUseCases.rebuild();
    }

    @PostMapping("/products/{productId}/retry")
    /**
     * Retry.
     * @param productId productId.
     */
    public IndexResponse retry(@PathVariable UUID productId) {
        return mapper.toIndexResponse(indexUseCases.retryIndexing(ProductId.of(productId)));
    }

    @GetMapping("/failures")
    /**
     * Failures.
     */
    public List<IndexResponse> failures() {
        return indexUseCases.listIndexFailures().stream()
                .map(mapper::toIndexResponse)
                .toList();
    }
}