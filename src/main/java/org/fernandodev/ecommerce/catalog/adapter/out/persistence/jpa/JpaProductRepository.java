package org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.fernandodev.ecommerce.catalog.adapter.out.persistence.jpa.entity.ProductJpaEntity;
import org.fernandodev.ecommerce.catalog.adapter.util.mapper.ProductPersistenceMapper;
import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.dto.PageResult;
import org.fernandodev.ecommerce.catalog.application.dto.ProductSearch;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.Barcode;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;

/**
 * Adaptador de salida que implementa {@link ProductRepository} sobre JPA.
 */
@Component
@RequiredArgsConstructor
public class JpaProductRepository implements ProductRepository {

    private final ProductSpringDataRepository springData;

    @Override
    public Product save(Product product) {
        ProductJpaEntity saved = springData.save(ProductPersistenceMapper.toEntity(product));
        return ProductPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return springData.findById(id.value()).map(ProductPersistenceMapper::toDomain);
    }

    @Override
    public boolean existsBySku(Sku sku) {
        return springData.existsBySkuAndDeletedAtIsNull(sku.value());
    }

    @Override
    public boolean existsByBarcode(Barcode barcode) {
        return barcode != null && springData.existsByBarcodeAndDeletedAtIsNull(barcode.value());
    }

    @Override
    public boolean existsByBrandId(BrandId brandId) {
        return springData.existsByBrandIdAndDeletedAtIsNull(brandId.value());
    }

    @Override
    public boolean existsByCategoryId(CategoryId categoryId) {
        return springData.existsByCategoryIdAndDeletedAtIsNull(categoryId.value());
    }

    @Override
    public PageResult<Product> search(ProductSearch search, PageQuery query) {
        Page<ProductJpaEntity> page = springData.findAll(toSpecification(search), toPageable(query));
        List<Product> content = page.getContent().stream().map(ProductPersistenceMapper::toDomain).toList();
        return new PageResult<>(content, query.page(), query.size(), page.getTotalElements(), page.getTotalPages());
    }

    private Specification<ProductJpaEntity> toSpecification(ProductSearch search) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isNull(root.get("deletedAt")));
            if (search.active() != null) {
                predicates.add(cb.equal(root.get("active"), search.active()));
            }
            if (search.brandId() != null) {
                predicates.add(cb.equal(root.get("brandId"), search.brandId().value()));
            }
            if (search.categoryId() != null) {
                predicates.add(cb.isMember(search.categoryId().value(), root.get("categoryIds")));
            }
            if (search.query() != null && !search.query().isBlank()) {
                String like = "%" + search.query().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), like),
                        cb.like(cb.lower(root.get("sku")), like)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private PageRequest toPageable(PageQuery query) {
        return PageRequest.of(query.page(), query.size(), toSort(query));
    }

    private Sort toSort(PageQuery query) {
        String property = switch (query.sortBy().toLowerCase()) {
            case "createdat" -> "createdAt";
            case "sku" -> "sku";
            default -> "name";
        };
        Sort.Direction direction = "desc".equalsIgnoreCase(query.sortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
}
