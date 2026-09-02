package org.fernandodev.ecommerce.cart.adapter.util.mapper;

import org.fernandodev.ecommerce.cart.adapter.out.persistence.redis.CartDocument;
import org.fernandodev.ecommerce.cart.domain.Cart;
import org.fernandodev.ecommerce.cart.domain.value_object.AppliedCoupon;
import org.fernandodev.ecommerce.cart.domain.value_object.CartId;
import org.fernandodev.ecommerce.cart.domain.value_object.CartItem;
import org.fernandodev.ecommerce.cart.domain.value_object.CartItemId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductId;
import org.fernandodev.ecommerce.catalog.domain.value_object.ProductVariantId;
import org.fernandodev.ecommerce.shared.domain.Money;

/**
 * Mapper manual entre el agregado de dominio {@link Cart} y el documento
 * serializable {@link CartDocument}. Sin lógica de negocio.
 */
public final class CartPersistenceMapper {

    private CartPersistenceMapper() {
    }

    public static CartDocument toDocument(Cart cart) {
        return new CartDocument(
                cart.getId().value(),
                cart.getOwnerId(),
                cart.getUpdatedAt(),
                cart.getItems().stream()
                        .map(CartPersistenceMapper::toItemDocument)
                        .toList(),
                cart.getCoupon() == null ? null : toCouponDocument(cart.getCoupon()));
    }

    public static Cart toDomain(CartDocument document) {
        return Cart.crear(
                CartId.of(document.id()),
                document.ownerId(),
                document.items().stream()
                        .map(CartPersistenceMapper::toDomainItem)
                        .toList(),
                document.coupon() == null ? null : toDomainCoupon(document.coupon()),
                document.updatedAt());
    }

    private static CartDocument.Coupon toCouponDocument(AppliedCoupon coupon) {
        return new CartDocument.Coupon(
                coupon.code(),
                coupon.discount().amount(),
                coupon.discount().currencyCode());
    }

    private static AppliedCoupon toDomainCoupon(CartDocument.Coupon coupon) {
        return new AppliedCoupon(coupon.code(), Money.of(coupon.amount(), coupon.currency()));
    }

    private static CartDocument.Item toItemDocument(CartItem item) {
        return new CartDocument.Item(
                item.id().value(),
                item.productId().value(),
                item.variantId() == null ? null : item.variantId().value(),
                item.quantity(),
                item.unitPrice().amount(),
                item.unitPrice().currencyCode());
    }

    private static CartItem toDomainItem(CartDocument.Item item) {
        return new CartItem(
                CartItemId.of(item.id()),
                ProductId.of(item.productId()),
                item.variantId() == null ? null : ProductVariantId.of(item.variantId()),
                item.quantity(),
                Money.of(item.amount(), item.currency()));
    }
}