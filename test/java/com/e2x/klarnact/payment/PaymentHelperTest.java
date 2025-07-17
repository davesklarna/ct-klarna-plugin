package com.e2x.klarnact.payment;

import com.commercetools.api.models.payment.*;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.FieldContainer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.e2x.klarnact.CustomFields.Payment.KLARNA_AUTH_TOKEN;
import static com.e2x.klarnact.PaymentProducer.getPayment;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaymentHelperTest {
    @Test
    void returnsTransactionsOfInitialChargeOnly() {
        final var transactions = List.of(
                Transaction.builder()
                        .type(TransactionType.CHARGE)
                        .state(TransactionState.INITIAL)
                        .build(),
                Transaction.builder()
                        .type(TransactionType.CHARGE)
                        .state(TransactionState.SUCCESS)
                        .build(),
                Transaction.builder()
                        .type(TransactionType.AUTHORIZATION)
                        .state(TransactionState.INITIAL)
                        .build(),
                Transaction.builder()
                        .type(TransactionType.CHARGE)
                        .state(TransactionState.FAILURE)
                        .build()
        );
        final Payment payment = Payment.builder().transactions(transactions).build();

        assertEquals(1, PaymentHelper.getCaptureRequests(payment).count());
    }

    @ParameterizedTest
    @MethodSource
    void returnsEmptyStreamWhenNoTransactions(Payment payment) {
        final var transactions = PaymentHelper.getCaptureRequests(payment);
        assertEquals(0, transactions.count());
    }

    private static Stream<Payment> returnsEmptyStreamWhenNoTransactions() {
        return Stream.of(
                Payment.builder()
                        .build(),
                Payment.builder()
                        .transactions(new ArrayList<>())
                        .build()
        );
    }

    @Test
    void returnsTransactionsOfRefundCorrectly() {
        final var transactions = List.of(
                Transaction.builder()
                        .type(TransactionType.REFUND)
                        .state(TransactionState.INITIAL)
                        .build(),
                Transaction.builder()
                        .type(TransactionType.REFUND)
                        .state(TransactionState.SUCCESS)
                        .build(),
                Transaction.builder()
                        .type(TransactionType.CHARGE)
                        .state(TransactionState.SUCCESS)
                        .build(),
                Transaction.builder()
                        .type(TransactionType.AUTHORIZATION)
                        .state(TransactionState.INITIAL)
                        .build(),
                Transaction.builder()
                        .type(TransactionType.AUTHORIZATION)
                        .state(TransactionState.SUCCESS)
                        .build(),
                Transaction.builder()
                        .type(TransactionType.REFUND)
                        .state(TransactionState.FAILURE)
                        .build(),
                Transaction.builder()
                        .type(TransactionType.CHARGE)
                        .state(TransactionState.FAILURE)
                        .build()
        );
        final Payment payment = Payment.builder().transactions(transactions).build();

        assertEquals(1, PaymentHelper.capturesOrRefunds(payment).count());
    }

    @ParameterizedTest
    @MethodSource
    void getLatestKlarnaPaymentShouldReturnMostRecentlyCreated(Payment p1, Payment p2) {
        final var optionalPayment1 = PaymentHelper.getLatestKlarnaPayment(List.of(
                PaymentReference.builder().obj(p1).build(),
                PaymentReference.builder().obj(p2).build()
        ));

        assertTrue(optionalPayment1.isPresent());
        assertEquals(p2, optionalPayment1.get().getObj());

        final var optionalPayment2 = PaymentHelper.getLatestKlarnaPayment(List.of(
                PaymentReference.builder().obj(p2).build(),
                PaymentReference.builder().obj(p1).build()
        ));

        assertTrue(optionalPayment2.isPresent());
        assertEquals(p2, optionalPayment2.get().getObj());
    }

    private static Stream<Arguments> getLatestKlarnaPaymentShouldReturnMostRecentlyCreated() {
        final Payment klarnaPayment = requireNonNull(getPayment());
        klarnaPayment.setCustom(CustomFields.builder()
                .fields(FieldContainer.builder()
                        .values(Map.of(KLARNA_AUTH_TOKEN, UUID.randomUUID().toString()))
                        .build())
                .build());

        return Stream.of(
                Arguments.of(
                        Payment.builder(klarnaPayment)
                                .createdAt(ZonedDateTime.of(LocalDate.now().minusDays(1).atStartOfDay(), ZoneId.systemDefault()))
                                .build(),
                        Payment.builder(klarnaPayment)
                                .createdAt(ZonedDateTime.of(LocalDate.now().atStartOfDay(), ZoneId.systemDefault()))
                                .build()
                ),
                Arguments.of(
                        Payment.builder(getPayment())
                                .createdAt(ZonedDateTime.of(LocalDate.now().atStartOfDay(), ZoneId.systemDefault()))
                                .build(),
                        Payment.builder(klarnaPayment)
                                .createdAt(ZonedDateTime.of(LocalDate.now().minusDays(1).atStartOfDay(), ZoneId.systemDefault()))
                                .build()
                )
        );
    }
}
