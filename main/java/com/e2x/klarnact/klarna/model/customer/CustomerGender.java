package com.e2x.klarnact.klarna.model.customer;

import com.fasterxml.jackson.annotation.JsonValue;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@RegisterForReflection
public enum CustomerGender {
    MALE("male"), FEMALE("female");

    private static final Map<String, CustomerGender> MAP;

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
        return this.code;
    }

    public CustomerGender from(String code) {
        if (code != null) {
            return MAP.get(code);
        } else return null;
    }
}
