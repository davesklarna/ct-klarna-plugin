package com.e2x.klarnact.klarna.model.order;

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
public enum AuthorisedPaymentMethodType {
    B2B_INVOICE("b2b_invoice"),
    BASE_ACCOUNT("base_account"),
    CARD("card"),
    DEFERRED_INTEREST("deferred_interest"),
    DIRECT_BANK_TRANSFER("direct_bank_transfer"),
    DIRECT_DEBIT("direct_debit"),
    FIXED_AMOUNT("fixed_amount"),
    INVOICE("invoice"),
    PAY_LATER_BY_CARD("pay_later_by_card"),
    PIX("pix"),
    SLICE_IT_BY_CARD("slice_it_by_card");

    private static final Map<String, AuthorisedPaymentMethodType> MAP;

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

    public AuthorisedPaymentMethodType from(String code) {
        if (code != null) {
            return MAP.get(code);
        } else return null;
    }

}
