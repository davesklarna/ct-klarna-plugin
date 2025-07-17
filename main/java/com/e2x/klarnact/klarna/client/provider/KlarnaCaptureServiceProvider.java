package com.e2x.klarnact.klarna.client.provider;

import com.e2x.klarnact.klarna.client.KlarnaCaptureService;
import com.e2x.klarnact.klarna.zone.KlarnaZone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Map;

@ApplicationScoped
@RequiredArgsConstructor
@Slf4j
public class KlarnaCaptureServiceProvider {

    private final Map<String, KlarnaCaptureService> captureServiceMap;

    @Inject
    KlarnaZone klarnaZone;

    public KlarnaCaptureService get(String country) {
        return captureServiceMap.get(klarnaZone.zone(country));
    }
}
