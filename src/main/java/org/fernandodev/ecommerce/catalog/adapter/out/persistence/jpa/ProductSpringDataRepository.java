package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa;

import java.util.UUID;

import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data JPA del agregado {@link ProductJpaEntity}.
 */
public interface ProductSpringDataRepository
        extends JpaRepository<ProductJpaEntity, UUID>, JpaSpecificationExecutor<ProductJpaEntity> {

    boolean existsBySkuAndDeletedAtIsNull(String sku);

    boolean existsByBarcodeAndDeletedAtIsNull(String barcode);

    boolean existsByBrandIdAndDeletedAtIsNull(UUID brandId);

    @Query("select case when count(p) > 0 then true else false end "
            + "from ProductJpaEntity p join p.categoryIds c "
            + "where c = :categoryId and p.deletedAt is null")
    boolean existsByCategoryIdAndDeletedAtIsNull(@Param("categoryId") UUID categoryId);
}
