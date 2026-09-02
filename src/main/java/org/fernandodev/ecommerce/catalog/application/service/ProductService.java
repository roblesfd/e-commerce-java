package org.fernandodev.ecommerce.catalog.application.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.dto.PageResult;
import org.fernandodev.ecommerce.catalog.application.dto.ProductCommand;
import org.fernandodev.ecommerce.catalog.application.dto.ProductSearch;
import org.fernandodev.ecommerce.catalog.application.port.out.BrandRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.CategoryRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.fernandodev.ecommerce.catalog.domain.Product;
import org.fernandodev.ecommerce.catalog.domain.value_object.Barcode;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductAttribute;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductImage;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariant;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
import org.fernandodev.ecommerce.shared.domain.exception.ConflictException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Servicio de aplicación del contexto Product Catalog.
 * <p>
 * Orquesta los casos de uso de producto y aplica reglas de aplicación (unicidad
 * de SKU/barcode, validación de referencias a marca y categorías) sobre los
 * puertos de persistencia. No depende de Spring.
 * </p>
 */
@RequiredArgsConstructor
public class ProductService implements ProductUseCases {

    private final ProductRepository products;
    private final BrandRepository brands;
    private final CategoryRepository categories;

    @Override
    public Product create(ProductCommand command) {
        assertSkuAvailable(command.sku(), null);
        assertBarcodeAvailable(command.barcode(), null);
        BrandId brandId = validateBrand(command.brandId());
        Set<CategoryId> categoryIds = validateCategories(command.categoryIds());

        Product product = Product.create(
                ProductId.generate(),
                new Sku(command.sku()),
                barcode(command.barcode()),
                command.name(),
                command.description(),
                brandId);
        applyDetails(product, command);
        categoryIds.forEach(product::associateCategory);
        return products.save(product);
    }

    @Override
    public Product getById(ProductId id) {
        return getExisting(id);
    }

    @Override
    public PageResult<Product> list(ProductSearch search, PageQuery query) {
        return products.search(search, query);
    }

    @Override
    public Product update(ProductId id, ProductCommand command) {
        Product product = getExisting(id);
        assertSkuAvailable(command.sku(), product.getSku());
        assertBarcodeAvailable(command.barcode(), product.getBarcode());
        BrandId brandId = validateBrand(command.brandId());
        Set<CategoryId> categoryIds = validateCategories(command.categoryIds());

        product.updateSku(new Sku(command.sku()));
        product.updateBaseInfo(command.name(), command.description(), barcode(command.barcode()));
        applyDetails(product, command);
        replaceCategories(product, categoryIds);
        if (brandId != null) {
            product.assignBrand(brandId);
        } else {
            product.removeBrand();
        }
        return products.save(product);
    }

    @Override
    public Product activate(ProductId id) {
        Product product = getExisting(id);
        product.activate();
        return products.save(product);
    }

    @Override
    public Product deactivate(ProductId id) {
        Product product = getExisting(id);
        product.deactivate();
        return products.save(product);
    }

    @Override
    public void delete(ProductId id) {
        Product product = getExisting(id);
        product.delete();
        products.save(product);
    }

    private Product getExisting(ProductId id) {
        Product product = products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + id));
        if (product.isDeleted()) {
            throw new ResourceNotFoundException("Producto no encontrado: " + id);
        }
        return product;
    }

    private void applyDetails(Product product, ProductCommand command) {
        product.setAttributes(command.attributes().stream()
                .map(attribute -> new ProductAttribute(attribute.name(), attribute.value()))
                .toList());
        product.setImages(command.images().stream()
                .map(ProductImage::new)
                .toList());
        product.replaceVariants(command.variants().stream()
                .map(variant -> new ProductVariant(
                        ProductVariantId.generate(),
                        variant.sku() == null ? null : new Sku(variant.sku()),
                        variant.barcode() == null ? null : new Barcode(variant.barcode()),
                        variant.attributes().stream()
                                .map(attribute -> new ProductAttribute(attribute.name(), attribute.value()))
                                .toList()))
                .toList());
    }

    private void replaceCategories(Product product, Set<CategoryId> categoryIds) {
        product.getCategoryIds().forEach(product::disassociateCategory);
        categoryIds.forEach(product::associateCategory);
    }

    private void assertSkuAvailable(String sku, Sku current) {
        Sku normalized = new Sku(sku);
        if (normalized.equals(current)) {
            return;
        }
        if (products.existsBySku(normalized)) {
            throw new ConflictException("Ya existe un producto con el SKU: " + normalized);
        }
    }

    private void assertBarcodeAvailable(String barcode, Barcode current) {
        if (barcode == null) {
            return;
        }
        Barcode normalized = new Barcode(barcode);
        if (normalized.equals(current)) {
            return;
        }
        if (products.existsByBarcode(normalized)) {
            throw new ConflictException("Ya existe un producto con el barcode: " + normalized);
        }
    }

    private BrandId validateBrand(UUID brandId) {
        if (brandId == null) {
            return null;
        }
        BrandId id = BrandId.of(brandId);
        if (!brands.existsById(id)) {
            throw new ResourceNotFoundException("Marca no encontrada: " + id);
        }
        return id;
    }

    private Set<CategoryId> validateCategories(Set<UUID> categoryIds) {
        Set<CategoryId> result = new HashSet<>();
        for (UUID categoryId : categoryIds) {
            CategoryId id = CategoryId.of(categoryId);
            if (!categories.existsById(id)) {
                throw new ResourceNotFoundException("Categoría no encontrada: " + id);
            }
            result.add(id);
        }
        return result;
    }

    private static Barcode barcode(String value) {
        return value == null ? null : new Barcode(value);
    }
}
