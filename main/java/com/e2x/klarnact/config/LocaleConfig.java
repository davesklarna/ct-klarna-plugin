package com.e2x.klarnact.config;

import com.e2x.klarnact.mapper.OtherPaymentMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static java.lang.System.lineSeparator;
import static java.util.Map.copyOf;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public enum LocaleConfig {
    INSTANCE;

    private static final String NON_KLARNA_PAYMENT_KEY = "nonKlarnaPayment";
    private static final String SHIPPING_CHARGE_KEY = "shippingCharge";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JsonNode config;

    LocaleConfig() {
        JsonNode json;
        try (
                InputStream inputStream = requireNonNull(OtherPaymentMapper.class.getResourceAsStream(
                        "/locale-config.json"
                ));
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            String contents = reader.lines().collect(joining(lineSeparator()));
            json = objectMapper.readTree(contents);
        } catch (Exception e) {
            final Logger log = LoggerFactory.getLogger(this.getClass());
            log.error("Unable to read locale-config json file: " + lineSeparator(), e);
            json = JsonNodeFactory.instance.objectNode();
        }
        config = json;
    }

    @NonNull
    public Map<String, String> getNonKlarnaPayment() {
        final JsonNode nkp = config.get(NON_KLARNA_PAYMENT_KEY);
        if (nkp == null || nkp instanceof NullNode) {
            return Map.of();
        }
        return copyOf(objectMapper.convertValue(nkp, new TypeReference<>() {}));
    }

    @NonNull
    public Map<String, String> getShippingCharge() {
        final JsonNode nkp = config.get(SHIPPING_CHARGE_KEY);
        if (nkp == null || nkp instanceof NullNode) {
            return Map.of();
        }
        return copyOf(objectMapper.convertValue(nkp, new TypeReference<>() {}));
    }
}
