package com.e2x.klarnact;

import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.PaymentMethodInfo;
import org.junit.jupiter.api.Test;

import static com.e2x.klarnact.PaymentInterfaceName.KLARNA;
import static org.junit.jupiter.api.Assertions.*;

class PaymentInterfaceNameTest {

    @Test
    public void toStringEqualsCode() {
        assertEquals(KLARNA.getCode(), KLARNA.toString());
    }

    @Test
    public void noMethodInfoIsNotOfInterest() {
        assertTrue(KLARNA.notOfInterest(Payment.of()));
        assertTrue(KLARNA.notOfInterest(Payment.builder()
                .paymentMethodInfo(PaymentMethodInfo.builder()
                        .build())
                .build()));
    }

    @Test
    public void nonMatchingPaymentInterfaceNotOfInterest() {
        assertTrue(KLARNA.notOfInterest(Payment.builder()
                .paymentMethodInfo(PaymentMethodInfo.builder()
                        .paymentInterface("AnyOtherValue")
                        .build())
                .build()));
    }

    @Test
    public void matchingPaymentInterfaceIsOfInterest() {
        assertFalse(KLARNA.notOfInterest(Payment.builder()
                .paymentMethodInfo(PaymentMethodInfo.builder()
                        .paymentInterface(KLARNA.getCode())
                        .build())
                .build()));
    }
}