package com.e2x.klarnact.klarna.model.order;

import com.e2x.klarnact.mapper.OtherPaymentMapper;
import com.e2x.klarnact.mapper.ShippingChargeMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder(builderClassName = "OrderLineBuilder", toBuilder = true)
@JsonDeserialize(builder = OrderLine.OrderLineBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class OrderLine {
    private final String name;
    private final Integer quantity;
    private final Integer unitPrice;
    private final Integer totalAmount;
    private final String imageUrl;
    private final String merchantData;
    private final ProductIdentifiers productIdentifiers;
    private final String productUrl;
    private final String quantityUnit;
    private final String reference;
    private final Integer taxRate;
    private final Integer totalDiscountAmount;
    private final Integer totalTaxAmount;
    private final String type;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class OrderLineBuilder {
    }

    public static OrderLine otherPayment(String locale, int amount) {
        return OrderLine.builder()
                .name(OtherPaymentMapper.getDescription(locale))
                .type("store_credit")
                .quantity(1)
                .unitPrice(-amount)
                .totalAmount(-amount)
                .build();
    }

    public static OrderLine shippingCharge(String locale, int amount, int taxRate, int taxAmount) {
        return OrderLine.builder()
                .name(ShippingChargeMapper.getDescription(locale))
                .type("shipping_fee")
                .quantity(1)
                .unitPrice(amount)
                .totalAmount(amount)
                .taxRate(taxRate)
                .totalTaxAmount(taxAmount)
                .build();
    }
}
