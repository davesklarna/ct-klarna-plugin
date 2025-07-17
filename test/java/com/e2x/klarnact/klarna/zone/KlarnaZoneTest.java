package com.e2x.klarnact.klarna.zone;

import com.e2x.klarnact.klarna.config.KlarnaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KlarnaZoneTest {

    @Mock
    KlarnaConfig klarnaConfigMock;

    KlarnaZone testObj;

    @BeforeEach
    public void setup() {
        testObj = new KlarnaZone(klarnaConfigMock);

        var zoneMapping = new HashMap<String, String>();
        zoneMapping.put("country code 1", "zone 1");
        zoneMapping.put("country code 2", "zone 2");
        when(klarnaConfigMock.zoneMapping()).thenReturn(zoneMapping);
    }

    @Test
    void shouldLookupAndStoreZone() {
        assertEquals("zone 1", testObj.zone("country code 1"));
        assertEquals("zone 1", testObj.getCurrent());
        assertEquals("zone 2", testObj.zone("country code 2"));
        assertEquals("zone 2", testObj.getCurrent());
    }

}
