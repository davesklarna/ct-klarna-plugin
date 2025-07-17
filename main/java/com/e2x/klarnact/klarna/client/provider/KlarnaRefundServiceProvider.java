package com.e2x.klarnact.klarna.client.provider;

import com.e2x.klarnact.klarna.client.KlarnaRefundService;
import com.e2x.klarnact.klarna.zone.KlarnaZone;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class KlarnaRefundServiceProvider {

    private final Map<String, KlarnaRefundService> refundServiceMap;

    @Inject
    KlarnaZone klarnaZone;

    public KlarnaRefundService get(String country) {
        return refundServiceMap.get(klarnaZone.zone(country));
    }
}
