package org.fernandodev.ecommerce.search.adapter.out.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.fernandodev.ecommerce.search.adapter.out.persistence.jpa.entity.SearchDocumentJpaEntity;
import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data JPA del índice de búsqueda.
 */
public interface SearchDocumentSpringDataRepository
        extends JpaRepository<SearchDocumentJpaEntity, UUID>, JpaSpecificationExecutor<SearchDocumentJpaEntity> {

    List<SearchDocumentJpaEntity> findByIndexStatus(IndexStatus status);

    @Query("select d.productId from SearchDocumentJpaEntity d")
    List<UUID> findAllProductIds();

    @Query("select d.name from SearchDocumentJpaEntity d where lower(d.name) like :prefix order by d.name")
    List<String> findAutocompleteNames(@Param("prefix") String prefix, Pageable pageable);

    @Query("select d.name from SearchDocumentJpaEntity d where lower(d.name) like :q order by d.name")
    List<String> findSuggestionNames(@Param("q") String q, Pageable pageable);
}