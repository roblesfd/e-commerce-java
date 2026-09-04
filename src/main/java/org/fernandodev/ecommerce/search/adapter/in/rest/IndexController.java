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
 * Controlador REST para el mantenimiento y administración del índice
 * de búsqueda de productos.
 *
 * <p>Expone operaciones para indexar y eliminar productos, reconstruir
 * el índice completo, reintentar indexaciones fallidas y consultar los
 * documentos que presentan errores durante el proceso de indexación.</p>
 *
 * <p>El controlador delega la ejecución de las operaciones en
 * {@link IndexUseCases} y utiliza {@link SearchMapper} para transformar
 * los documentos del índice en las respuestas de la API.</p>
 */
@RestController
@RequestMapping("/api/v1/search/index")
@RequiredArgsConstructor
public class IndexController {

    /**
     * Casos de uso relacionados con la gestión y mantenimiento
     * del índice de búsqueda.
     */
    private final IndexUseCases indexUseCases;

    /**
     * Mapper encargado de convertir los documentos de búsqueda
     * en las respuestas expuestas por la API.
     */
    private final SearchMapper mapper;

    /**
     * Indexa un producto en el índice de búsqueda.
     *
     * <p>Si el producto no genera un {@link SearchDocument}, se interpreta
     * que debe ser eliminado del índice y se devuelve una respuesta
     * indicando dicha situación.</p>
     *
     * @param productId identificador único del producto que se desea indexar.
     * @return resultado de la operación de indexación.
     */
    @PostMapping("/products/{productId}")
    public IndexResponse index(@PathVariable UUID productId) {
        SearchDocument document = indexUseCases.indexProduct(ProductId.of(productId));

        if (document == null) {
            return new IndexResponse(
                    productId,
                    null,
                    "Eliminado del índice",
                    0
            );
        }

        return mapper.toIndexResponse(document);
    }

    /**
     * Elimina un producto del índice de búsqueda.
     *
     * <p>Esta operación únicamente afecta al índice de búsqueda y no
     * implica la eliminación del producto de la fuente de verdad
     * correspondiente al catálogo.</p>
     *
     * @param productId identificador único del producto que se desea
     *                  eliminar del índice.
     */
    @DeleteMapping("/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable UUID productId) {
        indexUseCases.removeProduct(ProductId.of(productId));
    }

    /**
     * Reconstruye el índice de búsqueda.
     *
     * <p>La operación permite volver a generar los documentos del índice
     * a partir de la información disponible en la fuente de verdad.</p>
     *
     * <p>La ejecución se delega al caso de uso correspondiente, por lo que
     * cualquier estrategia de procesamiento, actualización o reindexación
     * es responsabilidad de la capa de aplicación.</p>
     */
    @PostMapping("/rebuild")
    public void rebuild() {
        indexUseCases.rebuild();
    }

    /**
     * Reintenta la indexación de un producto cuya indexación anterior
     * haya fallado.
     *
     * <p>Esta operación permite continuar el proceso de sincronización
     * de un documento que se encuentra en un estado de indexación fallido.</p>
     *
     * @param productId identificador único del producto cuya indexación
     *                  se desea reintentar.
     * @return resultado de la operación de reindexación.
     */
    @PostMapping("/products/{productId}/retry")
    public IndexResponse retry(@PathVariable UUID productId) {
        return mapper.toIndexResponse(
                indexUseCases.retryIndexing(ProductId.of(productId)));
    }

    /**
     * Obtiene los documentos que presentan errores de indexación.
     *
     * <p>Estos documentos pueden utilizarse para identificar productos
     * cuya información no se encuentra correctamente sincronizada con
     * el índice y que potencialmente requieren un reintento.</p>
     *
     * @return lista de resultados correspondientes a las indexaciones
     *         que han fallado.
     */
    @GetMapping("/failures")
    public List<IndexResponse> failures() {
        return indexUseCases.listIndexFailures().stream()
                .map(mapper::toIndexResponse)
                .toList();
    }
}
