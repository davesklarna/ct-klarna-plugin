package com.e2x.klarnact.klarna.config;

import java.util.Map;
import java.util.Optional;

public class KlarnaConfigTest implements KlarnaConfig {
    @Override
    public Map<String, ZoneConfig> zone() {
        return null;
    }

    @Override
    public Map<String, String> zoneMapping() {
        return null;
    }

    @Override
    public UserAgent userAgent() {
        return new UserAgent() {
            @Override
            public String platformName() {
                return "pName";
            }

            @Override
            public String platformVersion() {
                return "pVersion";
            }

            @Override
            public Optional<String> moduleName() {
                return Optional.of("mName");
            }

            @Override
            public Optional<String> moduleVersion() {
                return Optional.of("mVersion");
            }
        };
    }
}
