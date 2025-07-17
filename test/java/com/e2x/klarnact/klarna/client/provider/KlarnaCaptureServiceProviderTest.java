package com.e2x.klarnact.klarna.client.provider;

import com.e2x.klarnact.klarna.client.KlarnaCaptureService;
import com.e2x.klarnact.klarna.zone.KlarnaZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KlarnaCaptureServiceProviderTest {

    @Mock
    KlarnaCaptureService klarnaCaptureServiceMock1;

    @Mock
    KlarnaCaptureService klarnaCaptureServiceMock2;

    @Mock
    KlarnaZone klarnaZoneMock;

    KlarnaCaptureServiceProvider testObj;

    @BeforeEach
    public void setup() {
        testObj = new KlarnaCaptureServiceProvider(Map.of("zone 1", klarnaCaptureServiceMock1, "zone 2", klarnaCaptureServiceMock2));
        testObj.klarnaZone = klarnaZoneMock;

        when(klarnaZoneMock.zone("country 1")).thenReturn("zone 1");
        when(klarnaZoneMock.zone("country 2")).thenReturn("zone 1");
        when(klarnaZoneMock.zone("country 3")).thenReturn("zone 2");
    }

    @Test
    void shouldGetServiceForCountry() {
        assertEquals(klarnaCaptureServiceMock1, testObj.get("country 1"));
        assertEquals(klarnaCaptureServiceMock1, testObj.get("country 2"));
        assertEquals(klarnaCaptureServiceMock2, testObj.get("country 3"));
    }

}
