package com.e2x.klarnact.commercetools.cart;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.payment.Payment;
import io.smallrye.mutiny.Uni;

public interface CartService {

    Uni<Cart> findCartForPayment(Payment payment);

    Uni<Cart> findCartById(String id);
}
