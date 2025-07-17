package com.e2x.klarnact.klarna.client.provider;

import com.e2x.klarnact.klarna.client.KlarnaPaymentService;
import com.e2x.klarnact.klarna.zone.KlarnaZone;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class KlarnaPaymentServiceProvider {

    private final Map<String, KlarnaPaymentService> paymentServiceMap;

    @Inject
    KlarnaZone klarnaZone;

    public KlarnaPaymentService get(String country) {
        return paymentServiceMap.get(klarnaZone.zone(country));
    }
}
