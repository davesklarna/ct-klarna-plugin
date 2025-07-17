package com.e2x.klarnact.payment.update;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.Transaction;
import com.e2x.klarnact.klarna.model.order.Capture;
import com.e2x.klarnact.mapper.OrderLineMapper;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import java.util.Objects;

import static com.e2x.klarnact.mapper.AddressMapper.map;

@ApplicationScoped
@RequiredArgsConstructor
public class CaptureMapper {
    private final OrderLineMapper orderLineMapper;

    public Capture mapToCapture(Order order, Transaction transaction) {
        final var builder = Capture.builder()
                .reference(transaction.getId())
                .capturedAmount(transaction.getAmount().getCentAmount().intValue())
                .billingAddress(map(order.getBillingAddress()))
                .shippingAddress(map(order.getShippingAddress()));

        if (Objects.equals(transaction.getAmount().getCentAmount(), order.getTotalPrice().getCentAmount())) {
            final var orderLines = orderLineMapper.fromLineItems(order.getLocale(), order.getLineItems());
            orderLines.addAll(orderLineMapper.fromCustomLineItems(order.getLocale(), order.getCustomLineItems()));
            builder.orderLines(orderLines);
        }

        return builder.build();
    }
}
