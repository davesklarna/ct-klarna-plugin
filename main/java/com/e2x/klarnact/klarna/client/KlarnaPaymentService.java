package com.e2x.klarnact.klarna.client;

import com.e2x.klarnact.klarna.config.KlarnaHeaderConfig;
import com.e2x.klarnact.klarna.model.order.Order;
import com.e2x.klarnact.klarna.model.order.OrderCreated;
import com.e2x.klarnact.klarna.model.payment.CreditSession;
import com.e2x.klarnact.klarna.model.payment.SessionCreated;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/payments/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient
@RegisterClientHeaders(KlarnaHeaderConfig.class)
public interface KlarnaPaymentService {

    @POST
    @Path("/sessions")
    Uni<SessionCreated> createSession(CreditSession creditSession);

    @POST
    @Path("/sessions/{sessionId}")
    Uni<Response> updateSession(@PathParam("sessionId") String sessionId, CreditSession creditSession);

    @POST
    @Path("/authorizations/{authToken}/order")
    Uni<OrderCreated> createOrder(@PathParam("authToken") String authToken, Order order);

    @DELETE
    @Path("/authorizations/{authToken}")
    Uni<Response> cancelAuthorisation(@PathParam("authToken") String authToken);
}
