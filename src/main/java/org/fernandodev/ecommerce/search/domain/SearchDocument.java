package org.fernandodev.ecommerce.search.domain;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.fernandodev.ecommerce.catalog.domain.value_object.CategoryId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.search.domain.value_object.IndexStatus;
import org.fernandodev.ecommerce.shared.domain.Money;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
/**
 * Documento del índice de búsqueda utilizado como read-model denormalizado
 * de un producto.
 *
 * <p>Este modelo representa la información del producto necesaria para
 * realizar búsquedas de forma eficiente y no constituye la fuente de
 * verdad del catálogo. Los datos originales pertenecen al contexto de
 * productos y este documento representa una proyección de dicha información.</p>
 *
 * <p>El documento mantiene además el estado de su indexación mediante
 * {@link IndexStatus}, permitiendo registrar errores y contabilizar los
 * intentos realizados cuando la sincronización con el índice de búsqueda
 * falla.</p>
 *
 * <p>La colección de categorías se mantiene mediante una copia defensiva
 * y se expone como una colección no modificable para evitar que código
 * externo pueda alterar directamente el estado interno del documento.</p>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchDocument {

    /**
     * Identificador único del producto representado por el documento.
     */
    private ProductId productId;

    /**
     * Nombre del producto utilizado como información indexable.
     */
    private String name;

    /**
     * Descripción del producto utilizada como información indexable.
     */
    private String description;

    /**
     * SKU del producto.
     */
    private String sku;

    /**
     * Código de barras asociado al producto.
     */
    private String barcode;

    /**
     * Atributos adicionales del producto representados en el documento
     * de búsqueda.
     */
    private String attributes;

    /**
     * Identificadores de las categorías a las que pertenece el producto.
     *
     * <p>El acceso generado por Lombok se deshabilita para este campo
     * porque la colección se expone mediante {@link #getCategoryIds()},
     * garantizando una vista no modificable.</p>
     */
    @Getter(AccessLevel.NONE)
    private Set<CategoryId> categoryIds;

    /**
     * Identificador de la marca asociada al producto.
     */
    private UUID brandId;

    /**
     * Precio actual del producto.
     */
    private Money price;

    /**
     * Indica si el producto se encuentra disponible para su venta.
     */
    private boolean available;

    /**
     * Indica si el producto se encuentra activo dentro del catálogo.
     */
    private boolean active;

    /**
     * Texto consolidado utilizado por el motor de búsqueda para realizar
     * búsquedas sobre diferentes atributos del producto.
     */
    private String searchableText;

    /**
     * Estado actual de la indexación del documento.
     */
    private IndexStatus indexStatus;

    /**
     * Último error registrado durante el proceso de indexación.
     *
     * <p>Es {@code null} cuando no existe un error registrado.</p>
     */
    private String lastError;

    /**
     * Número de intentos realizados para indexar o sincronizar el documento.
     */
    private int attempts;

    /**
     * Instante en el que el documento fue actualizado por última vez.
     */
    private Instant updatedAt;

    /**
     * Crea un nuevo documento de búsqueda en estado sincronizado.
     *
     * <p>Esta factoría se utiliza para construir una nueva proyección
     * del producto a partir de la información proporcionada por el
     * contexto de catálogo.</p>
     *
     * <p>La colección de categorías se copia defensivamente para evitar
     * que modificaciones posteriores sobre la colección recibida alteren
     * el estado interno del documento.</p>
     *
     * @param productId identificador del producto.
     * @param name nombre del producto.
     * @param description descripción del producto.
     * @param sku SKU del producto.
     * @param barcode código de barras del producto.
     * @param attributes atributos adicionales del producto.
     * @param categoryIds identificadores de las categorías del producto.
     * @param brandId identificador de la marca del producto.
     * @param price precio del producto.
     * @param available indica si el producto está disponible.
     * @param active indica si el producto está activo.
     * @param searchableText texto consolidado utilizado para las búsquedas.
     * @param at instante de actualización del documento.
     * @return un nuevo {@code SearchDocument} en estado {@link IndexStatus#SYNCED}.
     * @throws NullPointerException si {@code productId}, {@code name},
     *         {@code searchableText} o {@code at} son {@code null}.
     */
    public static SearchDocument create(
            ProductId productId,
            String name,
            String description,
            String sku,
            String barcode,
            String attributes,
            Set<CategoryId> categoryIds,
            UUID brandId,
            Money price,
            boolean available,
            boolean active,
            String searchableText,
            Instant at
    ) {
        SearchDocument document = new SearchDocument();
        document.productId = Objects.requireNonNull(productId, "productId no puede ser null");
        document.name = Objects.requireNonNull(name, "name no puede ser null");
        document.description = description;
        document.sku = sku;
        document.barcode = barcode;
        document.attributes = attributes;
        document.categoryIds = new HashSet<>(categoryIds == null ? Set.of() : categoryIds);
        document.brandId = brandId;
        document.price = price;
        document.available = available;
        document.active = active;
        document.searchableText = Objects.requireNonNull(
                searchableText,
                "searchableText no puede ser null"
        );
        document.indexStatus = IndexStatus.SYNCED;
        document.lastError = null;
        document.attempts = 0;
        document.updatedAt = Objects.requireNonNull(at, "at no puede ser null");
        return document;
    }

    /**
     * Reconstruye un documento de búsqueda con su estado completo.
     *
     * <p>Esta factoría está destinada principalmente a la reconstrucción
     * del read-model desde la persistencia, por lo que permite restaurar
     * tanto los datos del producto como el estado actual de indexación,
     * los errores registrados y el número de intentos.</p>
     *
     * <p>La colección de categorías se copia defensivamente para evitar
     * que la colección proporcionada por la persistencia pueda modificar
     * posteriormente el estado interno del documento.</p>
     *
     * @param productId identificador del producto.
     * @param name nombre del producto.
     * @param description descripción del producto.
     * @param sku SKU del producto.
     * @param barcode código de barras del producto.
     * @param attributes atributos adicionales del producto.
     * @param categoryIds identificadores de las categorías del producto.
     * @param brandId identificador de la marca del producto.
     * @param price precio del producto.
     * @param available indica si el producto está disponible.
     * @param active indica si el producto está activo.
     * @param searchableText texto consolidado utilizado para las búsquedas.
     * @param indexStatus estado actual de la indexación.
     * @param lastError último error registrado durante la indexación.
     * @param attempts número de intentos realizados.
     * @param updatedAt instante de la última actualización.
     * @return documento de búsqueda reconstruido con el estado proporcionado.
     * @throws NullPointerException si {@code productId}, {@code name},
     *         {@code searchableText}, {@code indexStatus} o {@code updatedAt}
     *         son {@code null}.
     */
    public static SearchDocument crear(
            ProductId productId,
            String name,
            String description,
            String sku,
            String barcode,
            String attributes,
            Set<CategoryId> categoryIds,
            UUID brandId,
            Money price,
            boolean available,
            boolean active,
            String searchableText,
            IndexStatus indexStatus,
            String lastError,
            int attempts,
            Instant updatedAt
    ) {
        SearchDocument document = new SearchDocument();
        document.productId = Objects.requireNonNull(productId, "productId no puede ser null");
        document.name = Objects.requireNonNull(name, "name no puede ser null");
        document.description = description;
        document.sku = sku;
        document.barcode = barcode;
        document.attributes = attributes;
        document.categoryIds = new HashSet<>(categoryIds == null ? Set.of() : categoryIds);
        document.brandId = brandId;
        document.price = price;
        document.available = available;
        document.active = active;
        document.searchableText = Objects.requireNonNull(
                searchableText,
                "searchableText no puede ser null"
        );
        document.indexStatus = Objects.requireNonNull(
                indexStatus,
                "indexStatus no puede ser null"
        );
        document.lastError = lastError;
        document.attempts = attempts;
        document.updatedAt = Objects.requireNonNull(
                updatedAt,
                "updatedAt no puede ser null"
        );
        return document;
    }

    /**
     * Obtiene los identificadores de las categorías asociadas al producto.
     *
     * <p>Se devuelve una vista no modificable de la colección interna
     * para preservar la encapsulación del documento.</p>
     *
     * @return conjunto no modificable de identificadores de categorías.
     */
    public Set<CategoryId> getCategoryIds() {
        return Collections.unmodifiableSet(categoryIds);
    }

    /**
     * Marca el documento como correctamente sincronizado con el índice.
     *
     * <p>Al sincronizar correctamente el documento se establece el estado
     * {@link IndexStatus#SYNCED}, se elimina el último error registrado
     * y se actualiza la fecha de modificación.</p>
     *
     * @param at instante en el que se confirma la sincronización.
     */
    public void markSynced(Instant at) {
        this.indexStatus = IndexStatus.SYNCED;
        this.lastError = null;
        this.updatedAt = at;
    }

    /**
     * Marca el documento como fallido durante el proceso de indexación.
     *
     * <p>La operación registra el error producido, incrementa el número
     * de intentos realizados y actualiza el instante de modificación.
     * El contador de intentos permite utilizar posteriormente el documento
     * como base para estrategias de reintento.</p>
     *
     * @param error descripción del error producido durante la indexación.
     * @param at instante en el que se produjo o registró el fallo.
     */
    public void markFailed(String error, Instant at) {
        this.indexStatus = IndexStatus.FAILED;
        this.lastError = error;
        this.attempts++;
        this.updatedAt = at;
    }
}
