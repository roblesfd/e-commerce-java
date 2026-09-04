package org.fernandodev.ecommerce.returns.adapter.config;

import org.fernandodev.ecommerce.returns.application.port.in.ReturnPolicyUseCases;
import org.fernandodev.ecommerce.returns.application.port.in.ReturnUseCases;
import org.fernandodev.ecommerce.returns.application.port.out.OrderPort;
import org.fernandodev.ecommerce.returns.application.port.out.PaymentPort;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnPolicyRepository;
import org.fernandodev.ecommerce.returns.application.port.out.ReturnRepository;
import org.fernandodev.ecommerce.returns.application.service.ReturnPolicyService;
import org.fernandodev.ecommerce.returns.application.service.ReturnService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensambla los servicios de aplicación del contexto Returns &amp; Refunds.
 */
@Configuration
public class ReturnConfig {

    @Bean
    public ReturnUseCases returnUseCases(ReturnRepository returns, ReturnPolicyRepository policies,
                                         OrderPort orders, PaymentPort payments) {
        return new ReturnService(returns, policies, orders, payments);
    }

    @Bean
    public ReturnPolicyUseCases returnPolicyUseCases(ReturnPolicyRepository policies) {
        return new ReturnPolicyService(policies);
    }
}
