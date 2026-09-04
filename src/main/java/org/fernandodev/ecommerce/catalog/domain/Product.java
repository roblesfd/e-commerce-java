package org.fernandodev.ecommerce.catalog.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.fernandodev.ecommerce.catalog.domain.value_object.Barcode;
import org.fernandodev.ecommerce.catalog.domain.value_object.BrandId;
import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductAttribute;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductImage;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariant;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.catalog.domain.value_object.Sku;
import org.fernandodev.ecommerce.shared.domain.exception.BusinessRuleViolationException;
import org.fernandodev.ecommerce.shared.domain.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Agregado raíz del contexto Product Catalog que representa un producto.
 * <p>
 * Posee sus variantes, atributos, imágenes, categorías y marca. El precio y el
 * inventario NO pertenecen a este agregado: se gestionan en los bounded
 * contexts Pricing e Inventory (spec §22).
 * </p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {

    private ProductId id;
    private Sku sku;
    private Barcode barcode;
    private String name;
    private String description;
    private boolean active;
    private BrandId brandId;
    @Getter(AccessLevel.NONE)
    private Set<CategoryId> categoryIds;
    @Getter(AccessLevel.NONE)
    private List<ProductAttribute> attributes;
    @Getter(AccessLevel.NONE)
    private List<ProductImage> images;
    @Getter(AccessLevel.NONE)
    private List<ProductVariant> variants;
    private Instant deletedAt;

    /**
     * Factoría completa con copia defensiva e invariantes. Usada por la
     * persistencia para reconstruir el agregado.
     *
     * @param id          identificador del producto.
     * @param sku         código de existencias (obligatorio).
     * @param barcode     código de barras (opcional).
     * @param name        nombre del producto.
     * @param description descripción (opcional).
     * @param active      si está activo para la venta.
     * @param brandId     marca (opcional).
     * @param categoryIds categorías asociadas.
     * @param attributes  atributos del producto.
     * @param images      imágenes del producto.
     * @param variants    variantes del producto.
     * @param deletedAt   fecha de baja (null si no está eliminado).
     * @return el producto reconstruido.
     * @throws NullPointerException                si {@code id} o {@code sku} son {@code null}.
     * @throws BusinessRuleViolationException      si el nombre es inválido.
     */
    public static Product crear(
            ProductId id,
            Sku sku,
            Barcode barcode,
            String name,
            String description,
            boolean active,
            BrandId brandId,
            Set<CategoryId> categoryIds,
            List<ProductAttribute> attributes,
            List<ProductImage> images,
            List<ProductVariant> variants,
            Instant deletedAt
    ) {
        Product product = new Product();
        product.id = Objects.requireNonNull(id, "id no puede ser null");
        product.sku = Objects.requireNonNull(sku, "sku no puede ser null");
        product.barcode = barcode;
        product.name = requireName(name);
        product.description = description;
        product.active = active;
        product.brandId = brandId;
        product.categoryIds = new HashSet<>(categoryIds == null ? Set.of() : categoryIds);
        product.attributes = new ArrayList<>(attributes == null ? List.of() : attributes);
        product.images = new ArrayList<>(images == null ? List.of() : images);
        product.variants = new ArrayList<>(variants == null ? List.of() : variants);
        product.deletedAt = deletedAt;
        return product;
    }

    /**
     * Crea un nuevo producto activo sin colecciones asociadas.
     *
     * @param id          identificador del producto.
     * @param sku         código de existencias (obligatorio).
     * @param barcode     código de barras (opcional).
     * @param name        nombre del producto.
     * @param description descripción (opcional).
     * @param brandId     marca (opcional).
     * @return el producto creado.
     * @throws BusinessRuleViolationException si el nombre es inválido.
     */
    public static Product create(
            ProductId id,
            Sku sku,
            Barcode barcode,
            String name,
            String description,
            BrandId brandId
    ) {
        return crear(id, sku, barcode, name, description, true, brandId,
                Set.of(), List.of(), List.of(), List.of(), null);
    }

    /**
     * @return las categorías del producto como conjunto inmutable.
     */
    public Set<CategoryId> getCategoryIds() {
        return Collections.unmodifiableSet(categoryIds);
    }

    /**
     * @return los atributos del producto como lista inmutable.
     */
    public List<ProductAttribute> getAttributes() {
        return Collections.unmodifiableList(attributes);
    }

    /**
     * @return las imágenes del producto como lista inmutable.
     */
    public List<ProductImage> getImages() {
        return Collections.unmodifiableList(images);
    }

    /**
     * @return las variantes del producto como lista inmutable.
     */
    public List<ProductVariant> getVariants() {
        return Collections.unmodifiableList(variants);
    }

    /**
     * @return la fecha de baja, o {@code Optional.empty()} si no está eliminado.
     */
    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    /**
     * @return {@code true} si el producto fue eliminado (soft delete).
     */
    public boolean isDeleted() {
        return deletedAt != null;
    }

    /**
     * Actualiza la información base del producto.
     *
     * @param name        nombre del producto.
     * @param description descripción (opcional).
     * @param barcode     código de barras (opcional).
     * @throws BusinessRuleViolationException si el nombre es inválido.
     */
    public void updateBaseInfo(String name, String description, Barcode barcode) {
        this.name = requireName(name);
        this.description = description;
        this.barcode = barcode;
    }

    /**
     * @param sku nuevo código de existencias.
     * @throws NullPointerException si {@code sku} es {@code null}.
     */
    public void updateSku(Sku sku) {
        this.sku = Objects.requireNonNull(sku, "sku no puede ser null");
    }

    /**
     * Activa el producto para la venta.
     */
    public void activate() {
        this.active = true;
    }

    /**
     * Desactiva el producto.
     */
    public void deactivate() {
        this.active = false;
    }

    /**
     * Marca el producto como eliminado (soft delete).
     *
     * @throws BusinessRuleViolationException si el producto ya estaba eliminado.
     */
    public void delete() {
        if (isDeleted()) {
            throw new BusinessRuleViolationException("El producto ya está eliminado: " + id);
        }
        this.deletedAt = Instant.now();
    }

    /**
     * Añade una variante validando que su SKU/barcode no colisionen dentro del
     * agregado.
     *
     * @param sku        código de existencias de la variante (opcional).
     * @param barcode    código de barras de la variante (opcional).
     * @param attributes atributos de la variante.
     * @return la variante creada.
     * @throws BusinessRuleViolationException si el SKU o el barcode ya existen.
     */
    public ProductVariant addVariant(Sku sku, Barcode barcode, List<ProductAttribute> attributes) {
        ensureVariantSkuAvailable(sku, null);
        ensureVariantBarcodeAvailable(barcode, null);
        ProductVariant variant = new ProductVariant(ProductVariantId.generate(), sku, barcode, attributes);
        variants.add(variant);
        return variant;
    }

    /**
     * Actualiza una variante existente.
     *
     * @param id         identificador de la variante.
     * @param sku        código de existencias de la variante (opcional).
     * @param barcode    código de barras de la variante (opcional).
     * @param attributes atributos de la variante.
     * @throws BusinessRuleViolationException si el SKU o el barcode ya existen.
     * @throws ResourceNotFoundException      si la variante no pertenece al producto.
     */
    public void updateVariant(ProductVariantId id, Sku sku, Barcode barcode, List<ProductAttribute> attributes) {
        int index = variantIndex(id);
        ensureVariantSkuAvailable(sku, id);
        ensureVariantBarcodeAvailable(barcode, id);
        variants.set(index, new ProductVariant(id, sku, barcode, attributes));
    }

    /**
     * Elimina una variante del producto.
     *
     * @param id identificador de la variante.
     * @throws ResourceNotFoundException si la variante no pertenece al producto.
     */
    public void removeVariant(ProductVariantId id) {
        variants.remove(variantIndex(id));
    }

    /**
     * Reemplaza el conjunto completo de variantes validando la unicidad de
     * SKU/barcode entre ellas y frente a los del propio producto.
     *
     * @param variants nuevas variantes.
     * @throws BusinessRuleViolationException si hay SKU o barcode duplicados.
     */
    public void replaceVariants(List<ProductVariant> variants) {
        Set<Sku> skus = new HashSet<>();
        Set<Barcode> barcodes = new HashSet<>();
        if (this.sku != null) {
            skus.add(this.sku);
        }
        if (this.barcode != null) {
            barcodes.add(this.barcode);
        }
        for (ProductVariant variant : variants) {
            Objects.requireNonNull(variant, "variante no puede ser null");
            if (variant.sku() != null && !skus.add(variant.sku())) {
                throw new BusinessRuleViolationException("SKU duplicado: " + variant.sku());
            }
            if (variant.barcode() != null && !barcodes.add(variant.barcode())) {
                throw new BusinessRuleViolationException("Barcode duplicado: " + variant.barcode());
            }
        }
        this.variants = new ArrayList<>(variants);
    }

    /**
     * Añade o reemplaza un atributo por nombre.
     */
    /**
     * Añade o reemplaza un atributo por nombre.
     *
     * @param name  nombre del atributo.
     * @param value valor del atributo.
     * @throws IllegalArgumentException si el nombre o el valor son inválidos.
     */
    public void addAttribute(String name, String value) {
        ProductAttribute attribute = new ProductAttribute(name, value);
        attributes.removeIf(existing -> existing.name().equals(name));
        attributes.add(attribute);
    }

    /**
     * Elimina el atributo por nombre.
     *
     * @param name nombre del atributo.
     */
    public void removeAttribute(String name) {
        attributes.removeIf(existing -> existing.name().equals(name));
    }

    /**
     * Reemplaza el conjunto completo de atributos.
     *
     * @param attributes nuevos atributos.
     */
    public void setAttributes(List<ProductAttribute> attributes) {
        this.attributes = new ArrayList<>(attributes == null ? List.of() : attributes);
    }

    /**
     * Añade una imagen al producto.
     *
     * @param url URL de la imagen.
     */
    public void addImage(String url) {
        images.add(new ProductImage(url));
    }

    /**
     * Elimina la imagen con la URL indicada.
     *
     * @param url URL de la imagen.
     */
    public void removeImage(String url) {
        images.removeIf(image -> image.url().equals(url));
    }

    /**
     * Reemplaza el conjunto completo de imágenes.
     *
     * @param images nuevas imágenes.
     */
    public void setImages(List<ProductImage> images) {
        this.images = new ArrayList<>(images == null ? List.of() : images);
    }

    /**
     * Asocia una categoría al producto.
     *
     * @param categoryId identificador de la categoría.
     * @throws NullPointerException si {@code categoryId} es {@code null}.
     */
    public void associateCategory(CategoryId categoryId) {
        categoryIds.add(Objects.requireNonNull(categoryId, "categoryId no puede ser null"));
    }

    /**
     * Desasocia una categoría del producto.
     *
     * @param categoryId identificador de la categoría.
     */
    public void disassociateCategory(CategoryId categoryId) {
        categoryIds.remove(categoryId);
    }

    /**
     * Asigna la marca del producto.
     *
     * @param brandId identificador de la marca.
     * @throws NullPointerException si {@code brandId} es {@code null}.
     */
    public void assignBrand(BrandId brandId) {
        this.brandId = Objects.requireNonNull(brandId, "brandId no puede ser null");
    }

    /**
     * Elimina la marca del producto.
     */
    public void removeBrand() {
        this.brandId = null;
    }

    /**
     * Busca el índice de la variante por identificador.
     *
     * @param id identificador de la variante.
     * @return el índice de la variante.
     * @throws ResourceNotFoundException si la variante no pertenece al producto.
     */
    private int variantIndex(ProductVariantId id) {
        for (int i = 0; i < variants.size(); i++) {
            if (variants.get(i).id().equals(id)) {
                return i;
            }
        }
        throw new ResourceNotFoundException("Variante no encontrada: " + id);
    }

    /**
     * Valida que el SKU no colisione con el del producto ni con otra variante.
     *
     * @param sku      SKU a validar.
     * @param excluded variante excluida de la comprobación (la que se actualiza).
     * @throws BusinessRuleViolationException si el SKU ya está en uso.
     */
    private void ensureVariantSkuAvailable(Sku sku, ProductVariantId excluded) {
        if (sku == null) {
            return;
        }
        if (sku.equals(this.sku)) {
            throw new BusinessRuleViolationException("El SKU de la variante coincide con el del producto: " + sku);
        }
        boolean exists = variants.stream()
                .filter(variant -> !variant.id().equals(excluded))
                .anyMatch(variant -> sku.equals(variant.sku()));
        if (exists) {
            throw new BusinessRuleViolationException("SKU duplicado en las variantes: " + sku);
        }
    }

    /**
     * Valida que el barcode no colisione con el del producto ni con otra variante.
     *
     * @param barcode  barcode a validar.
     * @param excluded variante excluida de la comprobación (la que se actualiza).
     * @throws BusinessRuleViolationException si el barcode ya está en uso.
     */
    private void ensureVariantBarcodeAvailable(Barcode barcode, ProductVariantId excluded) {
        if (barcode == null) {
            return;
        }
        if (barcode.equals(this.barcode)) {
            throw new BusinessRuleViolationException("El barcode de la variante coincide con el del producto: " + barcode);
        }
        boolean exists = variants.stream()
                .filter(variant -> !variant.id().equals(excluded))
                .anyMatch(variant -> barcode.equals(variant.barcode()));
        if (exists) {
            throw new BusinessRuleViolationException("Barcode duplicado en las variantes: " + barcode);
        }
    }

    /**
     * Valida que el nombre del producto sea obligatorio y no exceda 200 caracteres.
     *
     * @param name nombre a validar.
     * @return el nombre si es válido.
     * @throws BusinessRuleViolationException si el nombre está vacío o es demasiado largo.
     */
    private static String requireName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessRuleViolationException("El nombre del producto es obligatorio");
        }
        if (name.length() > 200) {
            throw new BusinessRuleViolationException("El nombre del producto es demasiado largo (máx. 200 caracteres)");
        }
        return name;
    }
}
