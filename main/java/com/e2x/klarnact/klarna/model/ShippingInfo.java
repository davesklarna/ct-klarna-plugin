package com.e2x.klarnact.klarna.model;

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
@Builder(builderClassName = "ShippingInfoBuilder", toBuilder = true)
@JsonDeserialize(builder = ShippingInfo.ShippingInfoBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class ShippingInfo {
    private final String shippingCompany;
    private final ShippingMethod shippingMethod;
    private final String trackingNumber;
    private final String trackingUri;
    private final String returnShippingCompany;
    private final String returnTrackingNumber;
    private final String returnTrackingUri;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class ShippingInfoBuilder {
    }
}
