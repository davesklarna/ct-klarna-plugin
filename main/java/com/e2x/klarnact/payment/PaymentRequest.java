package com.e2x.klarnact.payment;

import com.commercetools.api.models.payment.PaymentReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class PaymentRequest {
    private final String action;
    private final PaymentReference resource;

    public boolean invalid() {
        return resource == null || resource.getObj() == null;
    }
}
