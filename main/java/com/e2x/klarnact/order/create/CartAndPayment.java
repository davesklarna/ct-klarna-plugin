package com.e2x.klarnact.order.create;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.payment.Payment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CartAndPayment {
    private final Cart cart;
    private final Payment payment;
}
