package com.e2x.klarnact.commercetools.order;

import com.commercetools.api.models.order.Order;
import com.e2x.klarnact.commercetools.client.CtClient;
import com.e2x.klarnact.commercetools.order.data.OrderTestData;
import com.e2x.klarnact.exception.NotFoundException;
import com.e2x.klarnact.payment.PaymentResourceTestInt;
import io.smallrye.mutiny.Uni;

import static java.lang.String.format;

public class OrderServiceMock extends OrderServiceImpl {
    public OrderServiceMock(CtClient ctClient) {
        super(ctClient);
    }

    @Override
    public Uni<Order> findOrderById(String id) {
        if (PaymentResourceTestInt.ORDER_ID.equals(id)) {
            return Uni.createFrom().item(OrderTestData::getOrder);
        } else {
            return Uni.createFrom().failure(
                    new NotFoundException(format("Order Not Found with id of %s", id))
            );
        }
    }
}
