package com.e2x.klarnact.commercetools.cart;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.order.PaymentInfo;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.PaymentReference;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.FieldContainer;
import com.e2x.klarnact.commercetools.payment.PaymentUtils;
import com.e2x.klarnact.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.e2x.klarnact.CustomFields.Payment.KLARNA_AUTH_TOKEN;
import static com.e2x.klarnact.PaymentProducer.getPayment;
import static com.e2x.klarnact.commercetools.cart.data.CartTestData.getCart;
import static com.e2x.klarnact.order.create.CreateOrderResourceTestInt.CART_ID;
import static com.e2x.klarnact.payment.PaymentResourceTestInt.CART_ANONYMOUS_ID;
import static com.e2x.klarnact.payment.PaymentResourceTestInt.CART_CUSTOMER_ID;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class CartServiceMock extends CartServiceImpl {
    private final ObjectMapper objectMapper;

    public CartServiceMock(ObjectMapper objectMapper) {
        super(null);
        this.objectMapper = objectMapper;
    }

    @Override
    public Uni<Cart> findCartById(String id) {
        if (CART_ID.equals(id)) {
            return Uni.createFrom().item(() -> Cart.builder(requireNonNull(getCart()))
                    .paymentInfo(PaymentInfo.builder()
                            .payments(List.of(PaymentReference.builder()
                                    .obj(Payment.builder(requireNonNull(getPayment()))
                                            .custom(CustomFields.builder()
                                                    .fields(FieldContainer.builder()
                                                            .values(Map.of(
                                                                    KLARNA_AUTH_TOKEN,
                                                                    UUID.randomUUID().toString()
                                                            )).build()
                                                    ).build()
                                            ).build()
                                    ).build()
                            )).build()
                    ).build()
            );
        } else {
            return Uni.createFrom().failure(() -> new NotFoundException(format("Cart Not Found with id of %s", id)));
        }
    }

    @Override
    public Uni<Cart> findCartForPayment(Payment payment) {

        requireNonNull(payment);

        if (matchesCustomer(payment) || CART_ANONYMOUS_ID.equals(payment.getAnonymousId())) {
            return Uni.createFrom().item(() -> {
                        try {
                            return objectMapper.readValue(Paths.get("src/test/resources/Cart.json").toFile(), Cart.class);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
            );
        } else return Uni.createFrom().failure(() -> cartNotFoundException(payment));
    }

    public static NotFoundException cartNotFoundException(Payment payment) {
        final var link = PaymentUtils.findLinkToCart(payment);
        return new NotFoundException(format(
                "Cart Not Found with %s of %s",
                link.getLeft(),
                link.getRight()
        ));
    }

    private boolean matchesCustomer(Payment payment) {
        return payment.getCustomer() != null
                && CART_CUSTOMER_ID.equals(payment.getCustomer().getId());
    }
}
