package org.fernandodev.ecommerce.search.adapter.in.rest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.fernandodev.ecommerce.search.adapter.in.rest.dto.SearchResultsResponse;
import org.fernandodev.ecommerce.search.adapter.in.rest.mapper.SearchMapper;
import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.fernandodev.ecommerce.search.application.port.in.SearchUseCases;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

/**
 * Controlador REST para la búsqueda, autocompletado y sugerencias
 * de productos.
 *
 * <p>Expone los endpoints utilizados para realizar búsquedas de productos
 * aplicando filtros, ordenamiento y paginación, así como para obtener
 * términos de autocompletado y sugerencias de búsqueda.</p>
 *
 * <p>El controlador delega la ejecución de las operaciones de búsqueda
 * en {@link SearchUseCases} y utiliza {@link SearchMapper} para transformar
 * los resultados del dominio en las respuestas de la API.</p>
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    /**
     * Número máximo de resultados utilizado por defecto para las operaciones
     * de autocompletado y sugerencias cuando se proporciona un límite
     * inválido o menor o igual a cero.
     */
    private static final int DEFAULT_LIMIT = 8;

    /**
     * Casos de uso relacionados con la búsqueda y consulta de productos.
     */
    private final SearchUseCases searchUseCases;

    /**
     * Mapper encargado de convertir los resultados de búsqueda
     * en las respuestas expuestas por la API.
     */
    private final SearchMapper mapper;

    /**
     * Busca productos aplicando los filtros y criterios de ordenamiento
     * proporcionados.
     *
     * <p>La consulta admite búsqueda por texto, categoría, marca,
     * rango de precios, disponibilidad y atributos del producto.
     * También permite especificar el criterio de ordenamiento y los
     * parámetros de paginación.</p>
     *
     * <p>Los atributos se reciben como una lista de cadenas con el formato
     * {@code clave:valor} y son convertidos a un {@link Map} antes de
     * construir el objeto {@link SearchQuery}.</p>
     *
     * @param q texto utilizado para realizar la búsqueda.
     * @param categoryId identificador de la categoría por la que se desea
     *                   filtrar.
     * @param brandId identificador de la marca por la que se desea filtrar.
     * @param minPrice precio mínimo incluido en el rango de búsqueda.
     * @param maxPrice precio máximo incluido en el rango de búsqueda.
     * @param available indica si se deben incluir únicamente productos
     *                  disponibles.
     * @param attribute lista de filtros de atributos en formato
     *                  {@code clave:valor}.
     * @param sort criterio de ordenamiento de los resultados.
     * @param page número de página, comenzando desde {@code 0}.
     * @param size cantidad de resultados solicitados por página.
     * @return resultados de la búsqueda de productos.
     */
    @GetMapping("/products")
    public SearchResultsResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) List<String> attribute,
            @RequestParam(required = false) SearchQuery.SortBy sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        SearchQuery query = new SearchQuery(
                q,
                categoryId,
                brandId,
                minPrice,
                maxPrice,
                available,
                parseAttributes(attribute),
                sort,
                page,
                size
        );

        return mapper.toResponse(searchUseCases.search(query));
    }

    /**
     * Obtiene términos de autocompletado relacionados con una consulta.
     *
     * <p>El límite solicitado se normaliza para garantizar que se encuentre
     * dentro del rango permitido. Los valores menores o iguales a cero
     * utilizan {@link #DEFAULT_LIMIT}, mientras que los valores superiores
     * a {@code 20} se limitan a {@code 20}.</p>
     *
     * @param q texto parcial utilizado para generar las opciones
     *          de autocompletado.
     * @param limit cantidad máxima de resultados solicitados.
     * @return lista de términos de autocompletado.
     */
    @GetMapping("/autocomplete")
    public List<String> autocomplete(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "8") int limit) {

        return searchUseCases.autocomplete(
                q,
                limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, 20)
        );
    }

    /**
     * Obtiene sugerencias relacionadas con una consulta de búsqueda.
     *
     * <p>El límite solicitado se normaliza para evitar valores inválidos
     * y restringir la cantidad máxima de sugerencias retornadas por
     * la API.</p>
     *
     * <p>Los valores menores o iguales a cero utilizan
     * {@link #DEFAULT_LIMIT}, mientras que los valores superiores
     * a {@code 20} se reducen automáticamente a {@code 20}.</p>
     *
     * @param q texto utilizado como base para generar las sugerencias.
     * @param limit cantidad máxima de sugerencias solicitadas.
     * @return lista de sugerencias relacionadas con la consulta.
     */
    @GetMapping("/suggest")
    public List<String> suggest(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "8") int limit) {

        return searchUseCases.suggest(
                q,
                limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, 20)
        );
    }

    /**
     * Convierte los filtros de atributos recibidos desde la API en un
     * mapa de pares clave-valor.
     *
     * <p>Cada atributo debe utilizar el formato
     * {@code clave:valor}. Los elementos que no contienen un separador
     * válido son ignorados.</p>
     *
     * <p>Cuando no se proporcionan atributos, se devuelve un mapa vacío
     * inmutable.</p>
     *
     * @param attribute lista de atributos recibidos desde los parámetros
     *                  de la petición.
     * @return mapa que contiene los atributos válidos como pares
     *         clave-valor.
     */
    private Map<String, String> parseAttributes(List<String> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return Map.of();
        }

        Map<String, String> attributes = new HashMap<>();

        attribute.forEach(raw -> {
            int separator = raw.indexOf(':');

            if (separator > 0) {
                attributes.put(
                        raw.substring(0, separator),
                        raw.substring(separator + 1)
                );
            }
        });

        return attributes;
    }
}
