package com.e2x.klarnact.commercetools.order;

import com.commercetools.api.models.order.Order;
import io.smallrye.mutiny.Uni;

public interface OrderService {
    Uni<Order> findOrderById(String id);
}
