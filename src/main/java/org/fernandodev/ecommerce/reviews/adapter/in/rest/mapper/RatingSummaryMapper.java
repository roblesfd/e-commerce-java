package org.fernandodev.ecommerce.reviews.adapter.in.rest.mapper;

import org.fernandodev.ecommerce.reviews.adapter.in.rest.dto.RatingSummaryResponse;
import org.fernandodev.ecommerce.reviews.application.dto.RatingSummary;
import org.springframework.stereotype.Component;

/**
 * Mapeo entre {@link RatingSummary} y su DTO REST.
 */
@Component
public class RatingSummaryMapper {

    public RatingSummaryResponse toResponse(RatingSummary summary) {
        return new RatingSummaryResponse(summary.count(), summary.average(), summary.distribution());
    }
}