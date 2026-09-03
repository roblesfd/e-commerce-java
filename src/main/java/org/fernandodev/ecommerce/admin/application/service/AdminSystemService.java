package org.fernandodev.ecommerce.admin.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.fernandodev.ecommerce.admin.application.port.in.AdminSystemUseCases;
import org.fernandodev.ecommerce.admin.application.port.out.AuditRepository;
import org.fernandodev.ecommerce.admin.application.port.out.SystemConfigRepository;
import org.fernandodev.ecommerce.admin.domain.AuditEntry;
import org.fernandodev.ecommerce.admin.domain.SystemConfig;
import org.fernandodev.ecommerce.catalog.application.dto.ProductSearch;
import org.fernandodev.ecommerce.catalog.application.dto.PageQuery;
import org.fernandodev.ecommerce.catalog.application.port.in.BrandUseCases;
import org.fernandodev.ecommerce.catalog.application.port.in.CategoryUseCases;
import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnUseCases;
import org.fernandodev.ecommerce.reviews.application.port.in.ReviewUseCases;
import org.fernandodev.ecommerce.search.application.port.in.IndexUseCases;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * Configuración del sistema, consultas de auditoría y métricas operativas.
 */
@RequiredArgsConstructor
public class AdminSystemService implements AdminSystemUseCases {

    private final SystemConfigRepository configs;
    private final AuditRepository audit;
    private final AuditService auditService;
    private final CustomerUseCases customers;
    private final ProductUseCases products;
    private final CategoryUseCases categories;
    private final BrandUseCases brands;
    private final OrderUseCases orders;
    private final PaymentUseCases payments;
    private final ReturnUseCases returns;
    private final ReviewUseCases reviews;
    private final IndexUseCases index;

    @Override
    @Transactional
    public SystemConfig setConfig(String key, String value) {
        Optional<SystemConfig> existing = configs.findById(key);
        SystemConfig config = existing.orElseGet(() -> SystemConfig.create(key, value, Instant.now()));
        if (existing.isPresent()) {
            config.update(value, Instant.now());
        }
        auditService.record("SET_CONFIG", "config", null, key + "=" + value);
        return configs.save(config);
    }

    @Override
    public Optional<SystemConfig> getConfig(String key) {
        return configs.findById(key);
    }

    @Override
    public List<SystemConfig> listConfig() {
        return configs.findAll();
    }

    @Override
    public List<AuditEntry> listAudit(String action, UUID actorId) {
        if (action != null) {
            return audit.findByAction(action);
        }
        if (actorId != null) {
            return audit.findByActorId(actorId);
        }
        return audit.findAll();
    }

    @Override
    public Metrics metrics() {
        return new Metrics(
                customers.listAll().size(),
                products.list(ProductSearch.all(), new PageQuery(0, 1, null, null)).totalElements(),
                categories.list().size(),
                brands.list().size(),
                orders.listAll().size(),
                payments.listAll().size(),
                returns.listAll().size(),
                reviews.listForModeration().size());
    }

    @Override
    public int integrationFailures() {
        return index.listIndexFailures().size();
    }
}