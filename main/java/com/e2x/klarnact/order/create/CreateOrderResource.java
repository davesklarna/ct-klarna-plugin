package com.e2x.klarnact.order.create;

import com.commercetools.api.models.order.OrderUpdate;
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
public class CreateOrderResource {

    private final CreateOrderService createOrderService;

    @POST
    @Path("/create")
    public Uni<OrderUpdate> createOrder(OrderRequest createOrderRequest) {
        return createOrderService.createOrder(createOrderRequest);
    }
}
