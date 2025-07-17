package com.e2x.klarnact.klarna.client.producer;

import com.e2x.klarnact.klarna.client.*;
import io.quarkus.arc.AlternativePriority;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@Slf4j
@AlternativePriority(1)
public class KlarnaServiceMockProducer {

    @Inject KlarnaOrderServiceMock klarnaOrderServiceMock;
    @Inject KlarnaPaymentServiceMock klarnaPaymentServiceMock;

    @Produces
    @ApplicationScoped
    Map<String, KlarnaOrderService> orderServiceMap() throws URISyntaxException {
        var serviceMap = new HashMap<String, KlarnaOrderService>();
        serviceMap.put("EU", klarnaOrderServiceMock);
        return serviceMap;
    }

    @Produces
    @ApplicationScoped
    Map<String, KlarnaPaymentService> paymentServiceMap() throws URISyntaxException {
        var serviceMap = new HashMap<String, KlarnaPaymentService>();
        serviceMap.put("EU", klarnaPaymentServiceMock);
        return serviceMap;
    }

}
