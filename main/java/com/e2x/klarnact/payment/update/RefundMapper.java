package com.e2x.klarnact.payment.update;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.Transaction;
import com.e2x.klarnact.klarna.model.order.Refund;
import com.e2x.klarnact.mapper.OrderLineMapper;
import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;

@ApplicationScoped
@AllArgsConstructor
public class RefundMapper {
    private final OrderLineMapper orderLineMapper;

    public Refund mapToRefund(Order order, Transaction transaction) {
        final var builder = Refund.builder()
                .reference(transaction.getId())
                .refundedAmount(transaction.getAmount().getCentAmount().intValue());

        if (Objects.equals(transaction.getAmount().getCentAmount(), order.getTotalPrice().getCentAmount())) {
            final var orderLines = orderLineMapper.fromLineItems(order.getLocale(), order.getLineItems());
            orderLines.addAll(orderLineMapper.fromCustomLineItems(order.getLocale(), order.getCustomLineItems()));
            builder.orderLines(orderLines);
        }

        return builder.build();
    }
}
