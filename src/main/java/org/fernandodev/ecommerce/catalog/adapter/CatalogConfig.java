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

    @Bean
    public ProductUseCases productUseCases(
            ProductRepository products,
            BrandRepository brands,
            CategoryRepository categories
    ) {
        return new ProductService(products, brands, categories);
    }

    @Bean
    public CategoryUseCases categoryUseCases(
            CategoryRepository categories,
            ProductRepository products
    ) {
        return new CategoryService(categories, products);
    }

    @Bean
    public BrandUseCases brandUseCases(
            BrandRepository brands,
            ProductRepository products
    ) {
        return new BrandService(brands, products);
    }
}
