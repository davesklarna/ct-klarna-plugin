package com.e2x.klarnact.klarna.client;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.cart.CartBuilder;
import com.commercetools.api.models.order.PaymentInfo;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.PaymentReference;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.FieldContainer;
import com.e2x.klarnact.commercetools.order.data.OrderTestData;
import com.e2x.klarnact.klarna.model.order.Order;
import com.e2x.klarnact.order.OrderToOrderMapper;
import com.e2x.klarnact.order.create.CartAndPayment;
import io.quarkus.arc.AlternativePriority;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.e2x.klarnact.CustomFields.Payment.KLARNA_AUTH_TOKEN;
import static com.e2x.klarnact.PaymentProducer.getPayment;
import static com.e2x.klarnact.commercetools.cart.data.CartTestData.getCart;
import static java.util.Objects.requireNonNull;

@AlternativePriority(1)
@ApplicationScoped
public class KlarnaOrderServiceMock implements KlarnaOrderService {
    @Inject
    OrderToOrderMapper orderToOrderMapper;

    @Override
    public Uni<Order> findOrder(String orderId) {
        final String klarnaAuthToken = UUID.randomUUID().toString();
        final String klarnaOrderId = UUID.randomUUID().toString();
        final var payment = Payment.builder(requireNonNull(getPayment()))
                .custom(CustomFields.builder()
                        .fields(FieldContainer.builder()
                                .values(Map.of(
                                        KLARNA_AUTH_TOKEN, klarnaAuthToken))
                                .build())
                        .build())
                .build();
        final Cart cart = CartBuilder.of(requireNonNull(getCart()))
                .paymentInfo(PaymentInfo.builder()
                        .payments(List.of(PaymentReference.builder()
                                .obj(payment)
                                .build()))
                        .build())
                .build();

        return orderToOrderMapper.mapToOrder(requireNonNull(OrderTestData.getOrder()), new CartAndPayment(cart, payment))
                .map(order -> order.toBuilder().orderId(klarnaOrderId).build());
    }

    @Override
    public Uni<Response> releaseRemainingAuthorisation(String orderId) {
        return Uni.createFrom().item(Response.noContent().build());
    }

    @Override
    public Uni<Response> cancelOrder(String orderId) {
        return Uni.createFrom().item(Response.noContent().build());
    }
}
