package com.e2x.klarnact.klarna.client;

import com.e2x.klarnact.klarna.config.KlarnaHeaderConfig;
import com.e2x.klarnact.klarna.model.order.Capture;
import io.smallrye.mutiny.Uni;
import org.eclipse.microprofile.rest.client.annotation.RegisterClientHeaders;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/ordermanagement/v1/orders/{orderId}/captures")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RegisterRestClient
@RegisterClientHeaders(KlarnaHeaderConfig.class)
public interface KlarnaCaptureService {

    @POST
    @Path("")
    Uni<Response> createCapture(@PathParam("orderId") String orderId, Capture capture);
}
