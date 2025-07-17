package com.e2x.klarnact.commercetools.payment;

import com.commercetools.api.models.customer.CustomerReference;
import com.commercetools.api.models.payment.Payment;
import com.e2x.klarnact.exception.KlarnaCtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.ws.rs.core.Response;
import java.util.UUID;
import java.util.stream.Stream;

import static com.e2x.klarnact.commercetools.payment.PaymentUtils.ANONYMOUS_ID;
import static com.e2x.klarnact.commercetools.payment.PaymentUtils.CUSTOMER_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PaymentUtilsTest {

    @Test
    public void correctlyIdentifyCustomerIdSearch() {
        final String customerId = UUID.randomUUID().toString();
        final var customer = CustomerReference.builder()
                .id(customerId)
                .build();

        final var res = PaymentUtils.findLinkToCart(Payment.builder().customer(customer).build());
        assertEquals(CUSTOMER_ID, res.getLeft());
        assertEquals(customerId, res.getRight());
    }

    @Test
    public void correctlyIdentifyAnonymousIdSearch() {
        final String anonymousId = UUID.randomUUID().toString();

        final var res = PaymentUtils.findLinkToCart(Payment.builder().anonymousId(anonymousId).build());
        assertEquals(ANONYMOUS_ID, res.getLeft());
        assertEquals(anonymousId, res.getRight());
    }

    @ParameterizedTest
    @MethodSource
    public void throwsExceptionWhenNoParametersAvailable(Payment payment) {
        final KlarnaCtException exception = assertThrows(KlarnaCtException.class,
                () -> PaymentUtils.findLinkToCart(payment));

        assertEquals(Response.Status.BAD_REQUEST, exception.getStatus());
        assertEquals("No parameters available to match payment to cart", exception.getMessage());
    }

    private static Stream<Payment> throwsExceptionWhenNoParametersAvailable() {
        return Stream.of(
                Payment.builder().build(),
                Payment.builder().customer(CustomerReference.of()).build()
        );
    }

}