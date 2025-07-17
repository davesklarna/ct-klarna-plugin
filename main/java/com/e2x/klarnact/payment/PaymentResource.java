package com.e2x.klarnact.payment;

import com.commercetools.api.models.payment.PaymentUpdate;
import com.e2x.klarnact.payment.create.CreatePaymentService;
import com.e2x.klarnact.payment.update.UpdatePaymentService;
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
@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    private final CreatePaymentService createPaymentService;
    private final UpdatePaymentService updatePaymentService;

    @POST
    @Path("/create")
    public Uni<PaymentUpdate> createPayment(PaymentRequest createPaymentRequest) {
        return createPaymentService.createPayment(createPaymentRequest);
    }

    @POST
    @Path("/update")
    public Uni<PaymentUpdate> updatePayment(PaymentRequest updatePaymentRequest) {
        return updatePaymentService.updatePayment(updatePaymentRequest);
    }
}