package org.fernandodev.ecommerce.search.adapter.config;

import org.fernandodev.ecommerce.search.application.port.in.IndexUseCases;
import org.fernandodev.ecommerce.search.application.port.in.SearchUseCases;
import org.fernandodev.ecommerce.search.application.port.out.CatalogPort;
import org.fernandodev.ecommerce.search.application.port.out.InventoryPort;
import org.fernandodev.ecommerce.search.application.port.out.PricingPort;
import org.fernandodev.ecommerce.search.application.port.out.SearchDocumentRepository;
import org.fernandodev.ecommerce.search.application.service.IndexService;
import org.fernandodev.ecommerce.search.application.service.SearchService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los servicios de aplicación del contexto Search.
 */
@Configuration
public class SearchConfig {

    @Bean
    public SearchUseCases searchUseCases(SearchDocumentRepository index) {
        return new SearchService(index);
    }

    @Bean
    public IndexUseCases indexUseCases(SearchDocumentRepository index, CatalogPort catalog,
                                       PricingPort pricing, InventoryPort inventory) {
        return new IndexService(index, catalog, pricing, inventory);
    }
}
