package com.e2x.klarnact.config;

import com.e2x.klarnact.klarna.config.ZoneMapInitialiser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ZoneMapInitialiserTest {

    ZoneMapInitialiser testObj = new ZoneMapInitialiser();

    @Test
    public void shouldContainCountryCodes() {
        assertTrue(testObj.getPropertyNames().size() > 1);
        assertTrue(testObj.getPropertyNames().contains("klarna.zone-mapping.AU"));
    }

    @Test
    public void shouldMapCountryCodeToPlaceholder() {
        assertEquals("none", testObj.getValue("klarna.zone-mapping.AU"));
    }

}
