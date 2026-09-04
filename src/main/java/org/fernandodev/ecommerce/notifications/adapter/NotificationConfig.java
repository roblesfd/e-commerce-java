package org.fernandodev.ecommerce.notifications.adapter;

import java.time.Duration;
import java.util.List;

import org.fernandodev.ecommerce.notifications.adapter.out.providers.ChannelProvider;
import org.fernandodev.ecommerce.notifications.adapter.out.providers.NotificationProviderRouter;
import org.fernandodev.ecommerce.notifications.adapter.out.providers.ResilientNotificationProvider;
import org.fernandodev.ecommerce.notifications.application.port.in.NotificationUseCases;
import org.fernandodev.ecommerce.notifications.application.port.in.PreferencesUseCases;
import org.fernandodev.ecommerce.notifications.application.port.in.TemplateUseCases;
import org.fernandodev.ecommerce.notifications.application.port.out.NotificationProviderPort;
import org.fernandodev.ecommerce.notifications.application.port.out.NotificationRepository;
import org.fernandodev.ecommerce.notifications.application.port.out.PreferencesRepository;
import org.fernandodev.ecommerce.notifications.application.port.out.TemplateRepository;
import org.fernandodev.ecommerce.notifications.application.service.NotificationService;
import org.fernandodev.ecommerce.notifications.application.service.PreferencesService;
import org.fernandodev.ecommerce.notifications.application.service.TemplateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;

/**
 * Cablea los servicios de aplicación (independientes de Spring) del contexto
 * Notifications con sus puertos y la resiliencia del proveedor.
 */
@Configuration
public class NotificationConfig {

    /**

     * Configura un bean del contexto Notifications.

     */

    @Bean
    public Retry notificationProviderRetry() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(100))
                .build();
        return Retry.of("notificationProvider", config);
    }

    /**

     * Configura un bean del contexto Notifications.

     */

    @Bean
    public NotificationProviderPort notificationProvider(List<ChannelProvider> channelProviders,
                                                         Retry notificationProviderRetry) {
        NotificationProviderRouter router = new NotificationProviderRouter(channelProviders);
        return new ResilientNotificationProvider(router, notificationProviderRetry);
    }

    /**

     * Configura un bean del contexto Notifications.

     */

    @Bean
    public NotificationUseCases notificationUseCases(NotificationRepository notifications,
                                                     NotificationProviderPort notificationProvider) {
        return new NotificationService(notifications, notificationProvider);
    }

    /**

     * Configura un bean del contexto Notifications.

     */

    @Bean
    public TemplateUseCases templateUseCases(TemplateRepository templates) {
        return new TemplateService(templates);
    }

    /**

     * Configura un bean del contexto Notifications.

     */

    @Bean
    public PreferencesUseCases preferencesUseCases(PreferencesRepository preferences) {
        return new PreferencesService(preferences);
    }
}