package com.e2x.klarnact.klarna.client;

import com.e2x.klarnact.klarna.config.KlarnaHeaderConfig;
import com.e2x.klarnact.klarna.model.order.Order;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ordermanagement/v1/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient
@RegisterClientHeaders(KlarnaHeaderConfig.class)
public interface KlarnaOrderService {

    @GET
    @Path("{orderId}")
    Uni<Order> findOrder(@PathParam("orderId") String orderId);

    @POST
    @Path("{orderId}/release-remaining-authorization")
    Uni<Response> releaseRemainingAuthorisation(@PathParam("orderId") String orderId);

    @POST
    @Path("{orderId}/cancel")
    Uni<Response> cancelOrder(@PathParam("orderId") String orderId);

}
