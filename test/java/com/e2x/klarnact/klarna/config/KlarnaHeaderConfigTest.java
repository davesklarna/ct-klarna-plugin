package com.e2x.klarnact.klarna.config;

import com.e2x.klarnact.klarna.zone.KlarnaZone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KlarnaHeaderConfigTest {

    @Mock
    KlarnaConfig klarnaConfigMock;

    @Mock
    KlarnaZone klarnaZoneMock;

    @Mock
    ZoneConfig zoneConfigMock;

    @Mock
    AuthConfig authConfigMock;

    @Mock
    UserAgent userAgentMock;

    KlarnaHeaderConfig testObj;

    @BeforeEach
    public void setup() {
        testObj = new KlarnaHeaderConfig(klarnaConfigMock);
        testObj.klarnaZone = klarnaZoneMock;

        when(klarnaZoneMock.getCurrent()).thenReturn("zone 1");
        var zoneConfigMap = Map.of("zone 1", zoneConfigMock);
        when(klarnaConfigMock.zone()).thenReturn(zoneConfigMap);
        when(zoneConfigMock.auth()).thenReturn(authConfigMock);
        when(authConfigMock.username()).thenReturn("username");
        when(authConfigMock.password()).thenReturn("password");
        when(klarnaConfigMock.userAgent()).thenReturn(userAgentMock);
        when(userAgentMock.userAgentString()).thenReturn("user agent string");
    }

    @Test
    void shouldCreateHeaders() {
        var result = testObj.update(null, null);

        assertEquals(4, result.size());
        assertEquals(List.of("application/json"), result.get("Content-Type"));
        assertEquals(List.of("application/json"), result.get("Accept"));
        assertEquals(List.of("Basic " + Base64.getEncoder().encodeToString("username:password".getBytes())), result.get("Authorization"));
        assertEquals(List.of("user agent string"), result.get("User-Agent"));
    }

}
