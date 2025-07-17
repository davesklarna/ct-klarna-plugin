package com.e2x.klarnact;

import com.commercetools.api.models.payment.Payment;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum PaymentInterfaceName {
    KLARNA("Klarna");

    @JsonValue
    @Getter
    private final String code;

    @Override
    public String toString() {
        return code;
    }

    public boolean notOfInterest(Payment payment) {
        return payment.getPaymentMethodInfo() == null ||
                !code.equals(payment.getPaymentMethodInfo().getPaymentInterface());
    }
}
