package org.fernandodev.ecommerce.checkout.adapter.util.mapper;

import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.checkout.adapter.out.persistence.redis.CheckoutDocument;
import org.fernandodev.ecommerce.checkout.domain.Checkout;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutId;
import org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutItem;
import org.fernandodev.ecommerce.checkout.domain.value_object.ReservationReference;
import org.fernandodev.ecommerce.order.domain.value_object.OrderId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Mapper manual entre el agregado de dominio {@link Checkout} y el documento
 * serializable {@link CheckoutDocument}. Sin lógica de negocio.
 */
public final class CheckoutPersistenceMapper {

    private CheckoutPersistenceMapper() {
    }

    /**
     * @param checkout agregado de dominio.
     * @return documento serializable equivalente.
     */
    public static CheckoutDocument toDocument(Checkout checkout) {
        return new CheckoutDocument(
                checkout.getId().value(),
                checkout.getCustomerId(),
                checkout.getIdempotencyKey(),
                checkout.getCartId(),
                checkout.getAddressId(),
                checkout.getAddressSnapshot(),
                checkout.getItems().stream().map(CheckoutPersistenceMapper::toItemDocument).toList(),
                checkout.getReservations().stream().map(CheckoutPersistenceMapper::toReservationDocument).toList(),
                checkout.getCouponCode(),
                checkout.getCouponDiscount() == null ? null : checkout.getCouponDiscount().amount(),
                checkout.getCouponDiscount() == null ? null : checkout.getCouponDiscount().currencyCode(),
                checkout.getSubtotal().amount(),
                checkout.getDiscounts().amount(),
                checkout.getTotal().amount(),
                checkout.getSubtotal().currencyCode(),
                checkout.getStatus().name(),
                checkout.getOrderId() == null ? null : checkout.getOrderId().value(),
                checkout.getPaymentId(),
                checkout.getCreatedAt(),
                checkout.getUpdatedAt());
    }

    /**
     * @param document documento serializable.
     * @return agregado de dominio equivalente.
     */
    public static Checkout toDomain(CheckoutDocument document) {
        return Checkout.crear(
                CheckoutId.of(document.id()),
                document.customerId(),
                document.idempotencyKey(),
                document.cartId(),
                document.addressId(),
                document.addressSnapshot(),
                document.items().stream().map(CheckoutPersistenceMapper::toDomainItem).toList(),
                document.reservations().stream().map(CheckoutPersistenceMapper::toDomainReservation).toList(),
                document.couponCode(),
                document.couponAmount() == null ? null
                        : Money.of(document.couponAmount(), document.couponCurrency()),
                Money.of(document.subtotalAmount(), document.currency()),
                Money.of(document.discountsAmount(), document.currency()),
                Money.of(document.totalAmount(), document.currency()),
                org.fernandodev.ecommerce.checkout.domain.value_object.CheckoutStatus.valueOf(document.status()),
                document.orderId() == null ? null : OrderId.of(document.orderId()),
                document.paymentId(),
                document.createdAt(),
                document.updatedAt());
    }

    private static CheckoutDocument.Item toItemDocument(CheckoutItem item) {
        return new CheckoutDocument.Item(
                item.productId().value(),
                item.variantId() == null ? null : item.variantId().value(),
                item.quantity(),
                item.unitPrice().amount(),
                item.unitPrice().currencyCode());
    }

    private static CheckoutDocument.Reservation toReservationDocument(ReservationReference reservation) {
        return new CheckoutDocument.Reservation(
                reservation.productId().value(),
                reservation.variantId() == null ? null : reservation.variantId().value(),
                reservation.reservationId());
    }

    private static CheckoutItem toDomainItem(CheckoutDocument.Item item) {
        return new CheckoutItem(
                ProductId.of(item.productId()),
                item.variantId() == null ? null : ProductVariantId.of(item.variantId()),
                item.quantity(),
                Money.of(item.amount(), item.currency()));
    }

    private static ReservationReference toDomainReservation(CheckoutDocument.Reservation reservation) {
        return new ReservationReference(
                ProductId.of(reservation.productId()),
                reservation.variantId() == null ? null : ProductVariantId.of(reservation.variantId()),
                reservation.reservationId());
    }
}