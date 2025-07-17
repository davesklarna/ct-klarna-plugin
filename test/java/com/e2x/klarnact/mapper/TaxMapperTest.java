package com.e2x.klarnact.mapper;

import com.commercetools.api.models.cart.TaxPortion;
import com.commercetools.api.models.cart.TaxedItemPrice;
import com.commercetools.api.models.cart.TaxedPrice;
import com.commercetools.api.models.common.CentPrecisionMoney;
import com.commercetools.api.models.tax_category.TaxRate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TaxMapperTest {

    @Test
    void orderTaxAmountNull() {
        assertNull(TaxMapper.orderTaxAmount(null));
    }

    @ParameterizedTest
    @MethodSource
    void itemTaxRate(TaxRate taxRate, Integer expected) {
        assertEquals(expected, TaxMapper.taxRate(taxRate));
    }

    @ParameterizedTest
    @MethodSource
    void orderTaxAmount(TaxedPrice taxedPrice, Integer expected) {
        assertEquals(expected, TaxMapper.orderTaxAmount(taxedPrice));
    }

    @ParameterizedTest
    @MethodSource
    void itemTaxAmount(TaxedItemPrice taxedItemPrice, Integer expected) {
        assertEquals(expected, TaxMapper.totalTaxAmount(taxedItemPrice));
    }

    private static Stream<Arguments> orderTaxAmount() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(TaxedPrice.builder().build(), null),
                Arguments.of(TaxedPrice.builder()
                        .taxPortions(TaxPortion.builder()
                                .amount(CentPrecisionMoney.builder()
                                        .currencyCode("EUR")
                                        .centAmount(1000L)
                                        .build())
                                .build())
                        .build(), 1000),
                Arguments.of(TaxedPrice.builder()
                        .taxPortions(TaxPortion.builder()
                                        .amount(CentPrecisionMoney.builder()
                                                .currencyCode("EUR")
                                                .centAmount(1000L)
                                                .build())
                                        .build(),
                                TaxPortion.builder()
                                        .amount(CentPrecisionMoney.builder()
                                                .currencyCode("EUR")
                                                .centAmount(1000L)
                                                .build())
                                        .build())
                        .build(), 2000)
        );
    }

    private static Stream<Arguments> itemTaxRate() {
        final double taxRate = 0.20;
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(TaxRate.builder().build(), null),
                Arguments.of(TaxRate.builder()
                        .amount(taxRate)
                        .build(), (int) (taxRate * 100 * 100))
        );
    }

    private static Stream<Arguments> itemTaxAmount() {
        final var gross = 120L;
        final var net = 100L;
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(TaxedItemPrice.builder().build(), null),
                Arguments.of(TaxedItemPrice.builder()
                        .totalGross(CentPrecisionMoney.builder().centAmount(gross).build())
                        .build(), null),
                Arguments.of(TaxedItemPrice.builder()
                        .totalNet(CentPrecisionMoney.builder().centAmount(net).build())
                        .build(), null),
                Arguments.of(TaxedItemPrice.builder()
                        .totalNet(CentPrecisionMoney.builder().centAmount(net).build())
                        .totalGross(CentPrecisionMoney.builder().centAmount(gross).build())
                        .build(), (int) (gross - net))
        );
    }
}
