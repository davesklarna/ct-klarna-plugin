package com.e2x.klarnact.order;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderBuilder;
import com.commercetools.api.models.order.PaymentInfo;
import com.commercetools.api.models.payment.*;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.FieldContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.e2x.klarnact.CustomFields.Order.EXTRA_MERCHANT_DATA;
import static com.e2x.klarnact.PaymentProducer.getPayment;
import static com.e2x.klarnact.commercetools.order.data.OrderTestData.getOrder;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

class OrderHelperTest {

    @Test
    void successfullyIdentifiesCapture() {
        final Order order = requireNonNull(getOrder());
        final Payment payment = requireNonNull(getPayment());
        payment.getTransactions().add(
                Transaction.builder()
                        .id(UUID.randomUUID().toString())
                        .type(TransactionType.CHARGE)
                        .state(TransactionState.SUCCESS)
                        .build()
        );
        final PaymentReference paymentReference = PaymentReference.builder()
                .id(payment.getId())
                .obj(payment)
                .build();
        order.setPaymentInfo(PaymentInfo.builder()
                .payments(List.of(paymentReference))
                .build());

        assertTrue(OrderHelper.hasCapture(order));
    }

    @ParameterizedTest
    @MethodSource
    void successfullyIdentifiesNoCapture(Order order) {
        assertFalse(OrderHelper.hasCapture(order));
    }

    private static Stream<Order> successfullyIdentifiesNoCapture() {
        final Order order = requireNonNull(getOrder());
        final Payment payment = requireNonNull(getPayment());
        payment.getTransactions().add(
                Transaction.builder()
                        .id(UUID.randomUUID().toString())
                        .type(TransactionType.AUTHORIZATION)
                        .state(TransactionState.FAILURE)
                        .build()
        );
        final PaymentReference paymentReference = PaymentReference.builder()
                .id(payment.getId())
                .obj(payment)
                .build();
        order.setPaymentInfo(PaymentInfo.builder()
                .payments(List.of(paymentReference))
                .build());

        return Stream.of(
                order,
                OrderBuilder.of(order)
                        .paymentInfo(null)
                        .build(),
                OrderBuilder.of(order)
                        .paymentInfo(PaymentInfo.of())
                        .build(),
                OrderBuilder.of(order)
                        .paymentInfo(PaymentInfo.builder()
                                .payments(PaymentReferenceBuilder.of(paymentReference)
                                        .id(payment.getId())
                                        .obj(Payment.builder()
                                                .build())
                                        .build())
                                .build())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource
    void successfullyRetrievesExtraMerchantData(String extraMerchantData) {
        final Map<String, Object> customFields = new HashMap<>();
        customFields.put(EXTRA_MERCHANT_DATA, extraMerchantData);

        final Order order = Order.builder().build();
        order.setCustom(CustomFields.builder()
                .fields(FieldContainer.builder()
                        .values(customFields)
                        .build())
                .build()
        );
        assertEquals(extraMerchantData, OrderHelper.getExtraMerchantData(order));
    }

    private static Stream<String> successfullyRetrievesExtraMerchantData() {
        return Stream.of(
                "{\"test\":\"data\"}",
                "",
                null
        );
    }

    @Test
    void nullReturnedRetrievingCustomFieldFromNullOrder() {
        assertNull(OrderHelper.getCustomField(null, "anyField", String.class));
    }
}
