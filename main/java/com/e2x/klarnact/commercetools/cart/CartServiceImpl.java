package com.e2x.klarnact.commercetools.cart;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.payment.Payment;
import com.e2x.klarnact.commercetools.client.CtClient;
import com.e2x.klarnact.exception.NotFoundException;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;

import static com.e2x.klarnact.commercetools.payment.PaymentUtils.findLinkToCart;
import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class CartServiceImpl implements CartService {
    private final CtClient ctClient;

    @Override
    public Uni<Cart> findCartForPayment(Payment payment) {
        final var where = findLinkToCart(payment);
        return Uni.createFrom().completionStage(() -> ctClient.request(requestBuilder ->
                requestBuilder.carts().get()
                        .withWhere(where.getLeft() + "=\"" + where.getRight() + "\" AND cartState=\"Active\"")
                        .withSort("createdAt desc")
                        .withLimit(1)
                        .execute()
                        .handleAsync((res, ex) -> {
                            if (res == null || res.getBody() == null || res.getBody().getResults().isEmpty()) {
                                throw new NotFoundException(format(
                                        "Cart Not Found with %s of %s",
                                        where.getLeft(),
                                        where.getRight()
                                ));
                            } else return res.getBody().getResults().get(0);
                        })));
    }

    @Override
    public Uni<Cart> findCartById(String id) {
        return Uni.createFrom().completionStage(() -> ctClient.request(requestBuilder ->
                requestBuilder.carts().withId(id)
                        .get()
                        .addExpand("paymentInfo.payments[*].obj")
                        .execute()
                        .handleAsync((res, ex) -> {
                            if (res == null || res.getBody() == null) {
                                throw new NotFoundException(format("Cart Not Found with id of %s", id));
                            } else return res.getBody();
                        })));
    }
}
