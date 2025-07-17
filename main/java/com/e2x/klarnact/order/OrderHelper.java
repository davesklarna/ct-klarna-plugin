package com.e2x.klarnact.order;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderUpdate;
import com.commercetools.api.models.payment.Transaction;
import com.commercetools.api.models.payment.TransactionState;
import com.commercetools.api.models.payment.TransactionType;
import com.e2x.klarnact.CustomFieldHelper;

import java.util.List;
import java.util.function.Predicate;

import static com.e2x.klarnact.CustomFields.Order.KLARNA_ORDER_ID;
import static com.e2x.klarnact.CustomFields.Order.EXTRA_MERCHANT_DATA;

public class OrderHelper {
    private static final Predicate<Transaction> SUCCESS_CAPTURE = t -> TransactionType.CHARGE == t.getType() && TransactionState.SUCCESS == t.getState();

    private OrderHelper() {
    }

    public static final OrderUpdate EMPTY = OrderUpdate.builder()
            .actions(List.of())
            .build();

    public static boolean hasCapture(Order order) {
        if (order.getPaymentInfo() == null || order.getPaymentInfo().getPayments() == null) return false;
        return order.getPaymentInfo().getPayments().stream()
                .filter(p -> p.getObj().getTransactions() != null)
                .flatMap(p -> p.getObj().getTransactions().stream())
                .anyMatch(SUCCESS_CAPTURE);
    }

    public static <T> T getCustomField(Order order, String fieldName, Class<T> clazz) {
        if (order == null) return null;
        return CustomFieldHelper.getCustomField(order.getCustom(), fieldName, clazz);
    }

    public static String getKlarnaOrderId(Order order) {
        return getCustomField(order, KLARNA_ORDER_ID, String.class);
    }

    public static String getExtraMerchantData(Order order) {
        return getCustomField(order, EXTRA_MERCHANT_DATA, String.class);
    }
}
