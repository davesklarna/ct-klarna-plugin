package com.e2x.klarnact.klarna.client.provider;

import com.e2x.klarnact.klarna.client.KlarnaRefundService;
import com.e2x.klarnact.klarna.zone.KlarnaZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KlarnaRefundServiceProviderTest {

    @Mock
    KlarnaRefundService klarnaRefundServiceMock1;

    @Mock
    KlarnaRefundService klarnaRefundServiceMock2;

    @Mock
    KlarnaZone klarnaZoneMock;

    KlarnaRefundServiceProvider testObj;

    @BeforeEach
    public void setup() {
        testObj = new KlarnaRefundServiceProvider(Map.of("zone 1", klarnaRefundServiceMock1, "zone 2", klarnaRefundServiceMock2));
        testObj.klarnaZone = klarnaZoneMock;

        when(klarnaZoneMock.zone("country 1")).thenReturn("zone 1");
        when(klarnaZoneMock.zone("country 2")).thenReturn("zone 1");
        when(klarnaZoneMock.zone("country 3")).thenReturn("zone 2");
    }

    @Test
    void shouldGetServiceForCountry() {
        assertEquals(klarnaRefundServiceMock1, testObj.get("country 1"));
        assertEquals(klarnaRefundServiceMock1, testObj.get("country 2"));
        assertEquals(klarnaRefundServiceMock2, testObj.get("country 3"));
    }

}
