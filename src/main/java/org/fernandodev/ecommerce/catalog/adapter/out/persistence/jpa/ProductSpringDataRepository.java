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

    /**
     * @param sku SKU.
     * @return {@code true} si ya existe un producto no eliminado con ese SKU.
     */
    boolean existsBySkuAndDeletedAtIsNull(String sku);

    /**
     * @param barcode código de barras.
     * @return {@code true} si ya existe un producto no eliminado con ese barcode.
     */
    boolean existsByBarcodeAndDeletedAtIsNull(String barcode);

    /**
     * @param brandId identificador de la marca.
     * @return {@code true} si existe algún producto no eliminado asociado a la marca.
     */
    boolean existsByBrandIdAndDeletedAtIsNull(UUID brandId);

    /**
     * @param categoryId identificador de la categoría.
     * @return {@code true} si existe algún producto no eliminado asociado a la categoría.
     */
    @Query("select case when count(p) > 0 then true else false end "
            + "from ProductJpaEntity p join p.categoryIds c "
            + "where c = :categoryId and p.deletedAt is null")
    boolean existsByCategoryIdAndDeletedAtIsNull(@Param("categoryId") UUID categoryId);
}
