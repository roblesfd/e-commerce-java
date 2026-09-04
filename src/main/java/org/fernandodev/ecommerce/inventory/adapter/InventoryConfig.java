package org.fernandodev.ecommerce.inventory.adapter;

import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.fernandodev.ecommerce.inventory.application.port.out.InventoryRepository;
import org.fernandodev.ecommerce.inventory.application.service.InventoryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea el servicio de aplicación (independiente de Spring) del contexto
 * Inventory con su puerto de persistencia.
 */
@Configuration
public class InventoryConfig {

    /**
     * @param inventoryRepository puerto de persistencia del inventario.
     * @return servicio de aplicación del contexto Inventory.
     */
    @Bean
    public InventoryUseCases inventoryUseCases(InventoryRepository inventoryRepository) {
        return new InventoryService(inventoryRepository);
    }
}