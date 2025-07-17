package com.e2x.klarnact.klarna.client.provider;

import com.e2x.klarnact.klarna.client.KlarnaPaymentService;
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
class KlarnaPaymentServiceProviderTest {

    @Mock
    KlarnaPaymentService klarnaPaymentServiceMock1;

    @Mock
    KlarnaPaymentService klarnaPaymentServiceMock2;

    @Mock
    KlarnaZone klarnaZoneMock;

    KlarnaPaymentServiceProvider testObj;

    @BeforeEach
    public void setup() {
        testObj = new KlarnaPaymentServiceProvider(Map.of("zone 1", klarnaPaymentServiceMock1, "zone 2", klarnaPaymentServiceMock2));
        testObj.klarnaZone = klarnaZoneMock;

        when(klarnaZoneMock.zone("country 1")).thenReturn("zone 1");
        when(klarnaZoneMock.zone("country 2")).thenReturn("zone 1");
        when(klarnaZoneMock.zone("country 3")).thenReturn("zone 2");
    }

    @Test
    void shouldGetServiceForCountry() {
        assertEquals(klarnaPaymentServiceMock1, testObj.get("country 1"));
        assertEquals(klarnaPaymentServiceMock1, testObj.get("country 2"));
        assertEquals(klarnaPaymentServiceMock2, testObj.get("country 3"));
    }

}
