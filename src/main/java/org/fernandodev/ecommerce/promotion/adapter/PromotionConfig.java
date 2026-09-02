package org.fernandodev.ecommerce.promotion.adapter;

import org.fernandodev.ecommerce.promotion.application.port.in.CouponUseCases;
import org.fernandodev.ecommerce.promotion.application.port.in.PromotionUseCases;
import org.fernandodev.ecommerce.promotion.application.port.out.CouponRepository;
import org.fernandodev.ecommerce.promotion.application.port.out.ProductCategoryProvider;
import org.fernandodev.ecommerce.promotion.application.port.out.PromotionRepository;
import org.fernandodev.ecommerce.promotion.application.service.CouponService;
import org.fernandodev.ecommerce.promotion.application.service.PromotionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cablea los servicios de aplicación (independientes de Spring) del contexto
 * Promotions & Coupons con sus puertos de persistencia.
 */
@Configuration
public class PromotionConfig {

    @Bean
    public PromotionUseCases promotionUseCases(PromotionRepository promotionRepository) {
        return new PromotionService(promotionRepository);
    }

    @Bean
    public CouponUseCases couponUseCases(
            CouponRepository couponRepository,
            PromotionRepository promotionRepository,
            ProductCategoryProvider productCategoryProvider
    ) {
        return new CouponService(couponRepository, promotionRepository, productCategoryProvider);
    }
}