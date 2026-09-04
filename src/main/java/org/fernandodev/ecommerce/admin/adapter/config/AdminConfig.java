package org.fernandodev.ecommerce.admin.adapter.config;

import org.fernandodev.ecommerce.admin.application.port.in.AdminCatalogUseCases;
import org.fernandodev.ecommerce.admin.application.port.in.AdminCustomerUseCases;
import org.fernandodev.ecommerce.admin.application.port.in.AdminInventoryUseCases;
import org.fernandodev.ecommerce.admin.application.port.in.AdminOrderUseCases;
import org.fernandodev.ecommerce.admin.application.port.in.AdminPaymentUseCases;
import org.fernandodev.ecommerce.admin.application.port.in.AdminPricingUseCases;
import org.fernandodev.ecommerce.admin.application.port.in.AdminPromotionUseCases;
import org.fernandodev.ecommerce.admin.application.port.in.AdminReturnUseCases;
import org.fernandodev.ecommerce.admin.application.port.in.AdminReviewUseCases;
import org.fernandodev.ecommerce.admin.application.port.in.AdminSystemUseCases;
import org.fernandodev.ecommerce.admin.application.port.out.AuditRepository;
import org.fernandodev.ecommerce.admin.application.port.out.CurrentActorPort;
import org.fernandodev.ecommerce.admin.application.port.out.SystemConfigRepository;
import org.fernandodev.ecommerce.admin.application.service.AdminCatalogService;
import org.fernandodev.ecommerce.admin.application.service.AdminCustomerService;
import org.fernandodev.ecommerce.admin.application.service.AdminInventoryService;
import org.fernandodev.ecommerce.admin.application.service.AdminOrderService;
import org.fernandodev.ecommerce.admin.application.service.AdminPaymentService;
import org.fernandodev.ecommerce.admin.application.service.AdminPricingService;
import org.fernandodev.ecommerce.admin.application.service.AdminPromotionService;
import org.fernandodev.ecommerce.admin.application.service.AdminReturnService;
import org.fernandodev.ecommerce.admin.application.service.AdminReviewService;
import org.fernandodev.ecommerce.admin.application.service.AdminSystemService;
import org.fernandodev.ecommerce.admin.application.service.AuditService;
import org.fernandodev.ecommerce.catalog.application.port.in.BrandUseCases;
import org.fernandodev.ecommerce.catalog.application.port.in.CategoryUseCases;
import org.fernandodev.ecommerce.catalog.application.port.in.ProductUseCases;
import org.fernandodev.ecommerce.customer.application.port.in.CustomerUseCases;
import org.fernandodev.ecommerce.inventory.application.port.in.InventoryUseCases;
import org.fernandodev.ecommerce.order.application.port.in.OrderUseCases;
import org.fernandodev.ecommerce.payment.application.port.in.PaymentUseCases;
import org.fernandodev.ecommerce.pricing.application.port.in.PricingUseCases;
import org.fernandodev.ecommerce.promotion.application.port.in.CouponUseCases;
import org.fernandodev.ecommerce.promotion.application.port.in.PromotionUseCases;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnUseCases;
import org.fernandodev.ecommerce.reviews.application.port.in.ReviewUseCases;
import org.fernandodev.ecommerce.search.application.port.in.IndexUseCases;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los servicios de orquestación administrativa.
 */
@Configuration
public class AdminConfig {

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AuditService auditService(AuditRepository audit, CurrentActorPort actor) {
        return new AuditService(audit, actor);
    }

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AdminCatalogUseCases adminCatalogUseCases(ProductUseCases products, CategoryUseCases categories,
                                                     BrandUseCases brands, AuditService auditService) {
        return new AdminCatalogService(products, categories, brands, auditService);
    }

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AdminPricingUseCases adminPricingUseCases(PricingUseCases pricing, AuditService auditService) {
        return new AdminPricingService(pricing, auditService);
    }

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AdminPromotionUseCases adminPromotionUseCases(PromotionUseCases promotions, CouponUseCases coupons,
                                                         AuditService auditService) {
        return new AdminPromotionService(promotions, coupons, auditService);
    }

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AdminInventoryUseCases adminInventoryUseCases(InventoryUseCases inventory, AuditService auditService) {
        return new AdminInventoryService(inventory, auditService);
    }

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AdminCustomerUseCases adminCustomerUseCases(CustomerUseCases customers) {
        return new AdminCustomerService(customers);
    }

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AdminOrderUseCases adminOrderUseCases(OrderUseCases orders, AuditService auditService) {
        return new AdminOrderService(orders, auditService);
    }

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AdminPaymentUseCases adminPaymentUseCases(PaymentUseCases payments, AuditService auditService) {
        return new AdminPaymentService(payments, auditService);
    }

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AdminReturnUseCases adminReturnUseCases(ReturnUseCases returns, AuditService auditService) {
        return new AdminReturnService(returns, auditService);
    }

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AdminReviewUseCases adminReviewUseCases(ReviewUseCases reviews, AuditService auditService) {
        return new AdminReviewService(reviews, auditService);
    }

    /**

     * Configura un bean de servicio de orquestación administrativa.

     */

    @Bean
    public AdminSystemUseCases adminSystemUseCases(
            SystemConfigRepository configs, AuditRepository audit, AuditService auditService,
            CustomerUseCases customers, ProductUseCases products, CategoryUseCases categories,
            BrandUseCases brands, OrderUseCases orders, PaymentUseCases payments,
            ReturnUseCases returns, ReviewUseCases reviews, IndexUseCases index) {
        return new AdminSystemService(configs, audit, auditService, customers, products, categories,
                brands, orders, payments, returns, reviews, index);
    }
}
