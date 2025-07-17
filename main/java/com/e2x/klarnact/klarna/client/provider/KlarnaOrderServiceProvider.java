package com.e2x.klarnact.klarna.client.provider;

import com.e2x.klarnact.klarna.client.KlarnaOrderService;
import com.e2x.klarnact.klarna.zone.KlarnaZone;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
public class KlarnaOrderServiceProvider {

    private final Map<String, KlarnaOrderService> orderServiceMap;

    @Inject
    KlarnaZone klarnaZone;

    public KlarnaOrderService get(String country) {
        return orderServiceMap.get(klarnaZone.zone(country));
    }
}
