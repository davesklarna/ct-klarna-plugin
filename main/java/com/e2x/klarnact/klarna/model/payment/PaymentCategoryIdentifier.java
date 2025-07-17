package com.e2x.klarnact.klarna.model.payment;

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
public enum PaymentCategoryIdentifier {
    DIRECT_BANK_TRANSFER("Direct_bank_transfer"),
    DIRECT_DEBIT("Direct_debit"),
    PAY_LATER("Pay_later"),
    PAY_NOW("Pay_now"),
    PAY_OVER_TIME("Pay_over_time");

    private static final Map<String, PaymentCategoryIdentifier> MAP;

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

    public PaymentCategoryIdentifier from(String code) {
        if (code != null) {
            return MAP.get(code);
        } else return null;
    }

}
