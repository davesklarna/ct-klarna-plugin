package com.e2x.klarnact.klarna.config;

import com.e2x.klarnact.klarna.zone.KlarnaZone;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Base64;

@RequiredArgsConstructor
@ApplicationScoped
public class KlarnaHeaderConfig implements ClientHeadersFactory {

    private final KlarnaConfig klarnaConfig;

    @Inject
    KlarnaZone klarnaZone;

    @Override
    public MultivaluedMap<String, String> update(
            MultivaluedMap<String, String> incomingHeaders,
            MultivaluedMap<String, String> clientOutgoingHeaders
    ) {
        MultivaluedMap<String, String> result = new MultivaluedHashMap<>();
        result.add("Content-Type", "application/json");
        result.add("Accept", "application/json");
        result.add("Authorization", getAuthHeaderValue());
        result.add("User-Agent", klarnaConfig.userAgent().userAgentString());
        return result;
    }

    private String getAuthHeaderValue() {
        final var auth = klarnaConfig.zone().get(klarnaZone.getCurrent()).auth();
        return "Basic " + base64(auth.username() + ":" + auth.password());
    }

    private String base64(final String value) {
        return Base64.getEncoder().encodeToString(value.getBytes());
    }
}
