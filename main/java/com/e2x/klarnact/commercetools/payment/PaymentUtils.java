package com.e2x.klarnact.commercetools.payment;

import com.commercetools.api.models.payment.Payment;
import com.e2x.klarnact.exception.KlarnaCtException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class PaymentUtils {
    public static final String ANONYMOUS_ID = "anonymousId";
    public static final String CUSTOMER_ID = "customerId";

    private PaymentUtils() {
    }

    public static Pair<String, String> findLinkToCart(Payment payment) {
        if (payment.getCustomer() != null && payment.getCustomer().getId() != null) {
            return new ImmutablePair<>(CUSTOMER_ID, payment.getCustomer().getId());
        } else if (!isBlank(payment.getAnonymousId())) {
            return new ImmutablePair<>(ANONYMOUS_ID, payment.getAnonymousId());
        } else {
            throw new KlarnaCtException("No parameters available to match payment to cart");
        }
    }
}
