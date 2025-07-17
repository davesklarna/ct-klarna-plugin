package com.e2x.klarnact.order.update;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderUpdate;
import com.e2x.klarnact.commercetools.order.OrderService;
import com.e2x.klarnact.exception.KlarnaCtException;
import com.e2x.klarnact.klarna.client.KlarnaOrderService;
import com.e2x.klarnact.klarna.client.provider.KlarnaOrderServiceProvider;
import com.e2x.klarnact.order.create.OrderRequest;
import io.smallrye.mutiny.Uni;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import javax.enterprise.context.ApplicationScoped;

import static com.commercetools.api.models.order.OrderState.CANCELLED;
import static com.e2x.klarnact.CustomFields.Order.KLARNA_ORDER_ID;
import static com.e2x.klarnact.order.OrderHelper.EMPTY;
import static com.e2x.klarnact.order.OrderHelper.hasCapture;
import static java.lang.String.format;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

@Slf4j
@ApplicationScoped
public class UpdateOrderService {

    private final OrderService orderService;
    private final KlarnaOrderServiceProvider klarnaOrderServiceProvider;

    public UpdateOrderService(
            OrderService orderService,
            KlarnaOrderServiceProvider klarnaOrderServiceProvider
    ) {
        this.orderService = orderService;
        this.klarnaOrderServiceProvider = klarnaOrderServiceProvider;
    }

    public Uni<OrderUpdate> updateOrder(OrderRequest updateOrderRequest) {

        if (!"update".equalsIgnoreCase(updateOrderRequest.getAction())) {
            throw new KlarnaCtException("Incorrect request type for endpoint");
        }

        final Order updated = updateOrderRequest.getResource().getObj();

        final String klarnaOrderId = updated.getCustom() == null ||
                updated.getCustom().getFields() == null ||
                updated.getCustom().getFields().values() == null ? null :
                (String) updated.getCustom().getFields().values().get(KLARNA_ORDER_ID);

        if (klarnaOrderId == null) {
            return Uni.createFrom().item(EMPTY);
        }

        return orderService.findOrderById(updated.getId())
                .flatMap(order -> updateOrder(order, updated, klarnaOrderId));
    }

    private Uni<OrderUpdate> updateOrder(Order original, Order updated, String klarnaOrderId) {
        if (CANCELLED == updated.getOrderState() && CANCELLED != original.getOrderState()) {
            if (hasCapture(original)) {
                throw new KlarnaCtException(format(
                        "Unable to cancel order %s as captures have been applied. Please use refund functionality.",
                        updated.getId()
                ));
            }
            return cancelOrder(updated, klarnaOrderId);
        }
        return Uni.createFrom().item(EMPTY);
    }

    private Uni<OrderUpdate> cancelOrder(Order updated, String klarnaOrderId) {
        return klarnaOrderServiceProvider.get(updated.getCountry()).cancelOrder(klarnaOrderId).map(r -> {
            if (SUCCESSFUL == r.getStatusInfo().getFamily()) {
                return EMPTY;
            } else {
                throw new KlarnaCtException(format("Unable to cancel order %s:%s", updated.getId(), klarnaOrderId));
            }
        });
    }
}
