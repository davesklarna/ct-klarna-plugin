package com.e2x.klarnact.klarna.zone;

import com.e2x.klarnact.klarna.config.KlarnaConfig;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.RequestScoped;

@RequestScoped
@RequiredArgsConstructor
public class KlarnaZone {

    private final KlarnaConfig klarnaConfig;

    @Getter
    private String current;

    public String zone(String country) {
        current = klarnaConfig.zoneMapping().get(country);
        return current;
    }
}
