package com.e2x.klarnact.klarna.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public enum ShippingMethod {
    BOX_REG("BoxReg"),
    BOX_UNREG("BoxUnreg"),
    DHL_PACKSTATION("DHLPackstation"),
    DIGITAL("Digital"),
    HOME("Home"),
    OWN("Own"),
    PICK_UP_POINT("PickUpPoint"),
    PICK_UP_STORE("PickUpStore"),
    POSTAL("Postal"),
    UNDEFINED("Undefined");

    private static final Map<String, ShippingMethod> MAP;

    static {
        MAP = Stream.of(values()).collect(toMap(
                it -> it.code.toLowerCase(),
                Function.identity()
        ));
    }

    @Getter
    @JsonValue
    private final String code;

    @Override
    public String toString() {
        return code;
    }

    public ShippingMethod from(String code) {
        if (code != null) {
            return MAP.get(code);
        } else return null;
    }
}
