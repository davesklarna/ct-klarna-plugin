package com.e2x.klarnact.order.update;

import com.commercetools.api.models.order.OrderUpdate;
import com.e2x.klarnact.order.create.OrderRequest;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Slf4j
@RequiredArgsConstructor
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UpdateOrderResource {

    private final UpdateOrderService updateOrderService;

    @POST
    @Path("/update")
    public Uni<OrderUpdate> updateOrder(OrderRequest createOrderRequest) {
        return updateOrderService.updateOrder(createOrderRequest);
    }
}
