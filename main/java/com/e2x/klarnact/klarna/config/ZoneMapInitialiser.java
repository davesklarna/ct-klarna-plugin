package com.e2x.klarnact.klarna.config;

import org.eclipse.microprofile.config.spi.ConfigSource;

import java.util.*;
import java.util.stream.Collectors;

// Populating a map entry via an environment variable is only possible if the entry already exists
// So initialise the zone mapping config with placeholder values for all countries
// These will then be overridden for valid countries in the yaml or environment variables
public class ZoneMapInitialiser implements ConfigSource {
    private static final Map<String, String> configuration;

    static {
        configuration = Arrays.stream(Locale.getISOCountries()).collect(Collectors.toMap(it -> "klarna.zone-mapping." + it, it -> "none"));
    }

    @Override
    public int getOrdinal() {
        return Integer.MIN_VALUE;
    }

    @Override
    public Set<String> getPropertyNames() {
        return configuration.keySet();
    }

    @Override
    public String getValue(final String propertyName) {
        return configuration.get(propertyName);
    }

    @Override
    public String getName() {
        return ZoneMapInitialiser.class.getSimpleName();
    }
}
