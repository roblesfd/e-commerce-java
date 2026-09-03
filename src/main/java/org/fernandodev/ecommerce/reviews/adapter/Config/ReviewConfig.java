package org.fernandodev.ecommerce.reviews.adapter.Config;

import org.fernandodev.ecommerce.reviews.application.port.in.ReviewUseCases;
import org.fernandodev.ecommerce.reviews.application.port.out.OrderPort;
import org.fernandodev.ecommerce.reviews.application.port.out.ReviewRepository;
import org.fernandodev.ecommerce.reviews.application.service.ReviewService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los servicios de aplicación del contexto Reviews.
 */
@Configuration
public class ReviewConfig {

    @Bean
    public ReviewUseCases reviewUseCases(ReviewRepository reviews, OrderPort orders) {
        return new ReviewService(reviews, orders);
    }
}