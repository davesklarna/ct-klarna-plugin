package com.e2x.klarnact.klarna.client.producer;

import com.e2x.klarnact.klarna.client.KlarnaCaptureService;
import com.e2x.klarnact.klarna.client.KlarnaOrderService;
import com.e2x.klarnact.klarna.client.KlarnaPaymentService;
import com.e2x.klarnact.klarna.client.KlarnaRefundService;
import com.e2x.klarnact.klarna.config.KlarnaConfig;
import com.e2x.klarnact.klarna.config.ZoneConfig;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.RestClientBuilder;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class KlarnaServiceProducer {

    private final KlarnaConfig klarnaConfig;

    @Produces
    @ApplicationScoped
    Map<String, KlarnaCaptureService> captureServiceMap() throws URISyntaxException {
        return serviceMap(KlarnaCaptureService.class);
    }

    @Produces
    @ApplicationScoped
    Map<String, KlarnaOrderService> orderServiceMap() throws URISyntaxException {
        return serviceMap(KlarnaOrderService.class);
    }

    @Produces
    @ApplicationScoped
    Map<String, KlarnaPaymentService> paymentServiceMap() throws URISyntaxException {
        return serviceMap(KlarnaPaymentService.class);
    }

    @Produces
    @ApplicationScoped
    Map<String, KlarnaRefundService> refundServiceMap() throws URISyntaxException {
        return serviceMap(KlarnaRefundService.class);
    }

    private <T> Map<String, T> serviceMap(Class<T> serviceClass) throws URISyntaxException {
        final var serviceMap = new HashMap<String,T>();

        for (Map.Entry<String, ZoneConfig> zoneEntry : klarnaConfig.zone().entrySet()) {
            final var service = RestClientBuilder.newBuilder()
                    .baseUri(new URI(zoneEntry.getValue().baseUrl()))
                    .build(serviceClass);
            serviceMap.put(zoneEntry.getKey(), service);
        }

        return serviceMap;
    }

}
