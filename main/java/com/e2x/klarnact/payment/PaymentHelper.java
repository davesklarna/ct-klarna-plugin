package com.e2x.klarnact.payment;

import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.PaymentReference;
import com.commercetools.api.models.payment.Transaction;
import com.e2x.klarnact.CustomFieldHelper;
import com.e2x.klarnact.exception.KlarnaCtException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.commercetools.api.models.payment.TransactionState.INITIAL;
import static com.commercetools.api.models.payment.TransactionState.SUCCESS;
import static com.commercetools.api.models.payment.TransactionType.*;
import static com.e2x.klarnact.CustomFields.Payment.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class PaymentHelper {
    private static final Predicate<Transaction> CAPTURE_REQUEST = t -> INITIAL == t.getState() && CHARGE == t.getType();
    private static final Predicate<Transaction> REFUND_REQUEST = t -> INITIAL == t.getState() && REFUND == t.getType();
    private static final Predicate<Transaction> CANCEL_AUTH_REQUEST = t -> INITIAL == t.getState() && CANCEL_AUTHORIZATION == t.getType();
    public static final Predicate<Transaction> CAPTURED_TRANSACTION = t -> SUCCESS == t.getState() && CHARGE == t.getType();

    private PaymentHelper() {
    }

    public static Payment extractPayment(PaymentRequest paymentRequest) {
        if (paymentRequest.invalid()) {
            throw new KlarnaCtException("Payment Reference must not be null");
        }
        return paymentRequest.getResource().getObj();
    }

    public static Stream<Transaction> getCaptureRequests(Payment payment) {
        if (payment.getTransactions() == null) return Stream.empty();
        return payment.getTransactions().stream()
                .filter(CAPTURE_REQUEST);
    }

    public static Stream<Transaction> capturesOrRefunds(Payment payment) {
        if (payment.getTransactions() == null) return Stream.empty();
        return payment.getTransactions().stream().filter(CAPTURE_REQUEST.or(REFUND_REQUEST));
    }

    public static Optional<Transaction> getCancelAuthTransaction(Payment payment) {
        if (payment.getTransactions() == null) return Optional.empty();
        return payment.getTransactions().stream()
                .filter(CANCEL_AUTH_REQUEST).findFirst();
    }

    public static <T> T getCustomField(Payment payment, String fieldName, Class<T> clazz) {
        if (payment == null) return null;
        return CustomFieldHelper.getCustomField(payment.getCustom(), fieldName, clazz);
    }

    public static String getOrderId(Payment payment) {
        return getCustomField(payment, ORDER_ID, String.class);
    }

    public static String getAuthToken(Payment payment) {
        return getCustomField(payment, KLARNA_AUTH_TOKEN, String.class);
    }

    public static String getSessionId(Payment payment) {
        return getCustomField(payment, SESSION_ID, String.class);
    }

    public static Optional<PaymentReference> getLatestKlarnaPayment(List<PaymentReference> payments) {
        if (payments == null) return Optional.empty();
        return payments.stream()
                .filter(p -> !isBlank(getAuthToken(p.getObj())))
                .max((p1, p2) -> {
                    if (Objects.equals(p1.getObj().getCreatedAt(), p2.getObj().getCreatedAt())) return 0;
                    return p1.getObj().getCreatedAt().isBefore(p2.getObj().getCreatedAt()) ? -1 : 1;
                });
    }
}
