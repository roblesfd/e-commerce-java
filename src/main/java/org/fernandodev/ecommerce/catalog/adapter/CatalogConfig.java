package org.fernandodev.ecommerce.catalog.adapter;

import org.fernandodev.ecommerce.catalog.application.service.BrandService;
import org.fernandodev.ecommerce.catalog.application.port.in.BrandUseCases;
import org.fernandodev.ecommerce.catalog.application.service.CategoryService;
import org.fernandodev.ecommerce.catalog.application.port.in.CategoryUseCases;
import org.fernandodev.ecommerce.catalog.application.service.ProductService;
import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.catalog.application.port.out.BrandRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.CategoryRepository;
import org.fernandodev.ecommerce.catalog.application.port.out.ProductRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea los servicios de aplicación (independientes de Spring) del contexto
 * Product Catalog con sus puertos de persistencia.
 */
@Configuration
public class CatalogConfig {

    /**
     * @param products  puerto de persistencia de productos.
     * @param brands    puerto de persistencia de marcas.
     * @param categories puerto de persistencia de categorías.
     * @return servicio de aplicación de productos.
     */
    @Bean
    public ProductUseCases productUseCases(
            ProductRepository products,
            BrandRepository brands,
            CategoryRepository categories
    ) {
        return new ProductService(products, brands, categories);
    }

    /**
     * @param categories puerto de persistencia de categorías.
     * @param products   puerto de persistencia de productos.
     * @return servicio de aplicación de categorías.
     */
    @Bean
    public CategoryUseCases categoryUseCases(
            CategoryRepository categories,
            ProductRepository products
    ) {
        return new CategoryService(categories, products);
    }

    /**
     * @param brands   puerto de persistencia de marcas.
     * @param products puerto de persistencia de productos.
     * @return servicio de aplicación de marcas.
     */
    @Bean
    public BrandUseCases brandUseCases(
            BrandRepository brands,
            ProductRepository products
    ) {
        return new BrandService(brands, products);
    }
}
