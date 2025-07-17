package com.e2x.klarnact.klarna.config;

import io.smallrye.config.ConfigMapping;

import java.util.Map;

@ConfigMapping(prefix = KlarnaConfig.PREFIX)
public interface KlarnaConfig {
    String PREFIX = "klarna";

    Map<String,ZoneConfig> zone();

    Map<String,String> zoneMapping();

    UserAgent userAgent();
}
