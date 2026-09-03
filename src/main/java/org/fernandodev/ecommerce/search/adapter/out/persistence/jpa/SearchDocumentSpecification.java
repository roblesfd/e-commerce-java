package org.fernandodev.ecommerce.search.adapter.out.persistence.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.search.adapter.out.persistence.jpa.entity.SearchDocumentJpaEntity;
import org.fernandodev.ecommerce.search.application.dto.SearchQuery;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Especificación JPA que construye los predicados de la búsqueda: texto libre
 * (ILIKE sobre el texto indexado, cubierto por trigram), filtros simultáneos
 * de categoría, marca, rango de precio, disponibilidad y atributos, y solo
 * documentos activos.
 */
public final class SearchDocumentSpecification implements Specification<SearchDocumentJpaEntity> {

    private final SearchQuery query;

    private SearchDocumentSpecification(SearchQuery query) {
        this.query = query;
    }

    public static SearchDocumentSpecification of(SearchQuery query) {
        return new SearchDocumentSpecification(query);
    }

    @Override
    public Predicate toPredicate(Root<SearchDocumentJpaEntity> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isTrue(root.get("active")));

        if (query.q() != null && !query.q().isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("searchableText")), "%" + query.q().toLowerCase() + "%"));
        }
        if (query.categoryId() != null) {
            predicates.add(cb.isMember(query.categoryId(), root.get("categoryIds")));
        }
        if (query.brandId() != null) {
            predicates.add(cb.equal(root.get("brandId"), query.brandId()));
        }
        if (query.minPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), query.minPrice()));
        }
        if (query.maxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), query.maxPrice()));
        }
        if (query.available() != null) {
            predicates.add(cb.equal(root.get("available"), query.available()));
        }
        query.attributes().forEach((name, value) ->
                predicates.add(cb.like(cb.lower(root.get("attributes")),
                        "%" + name.toLowerCase() + ":" + value.toLowerCase() + "%")));

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}