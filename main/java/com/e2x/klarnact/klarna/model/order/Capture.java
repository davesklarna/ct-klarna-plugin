package com.e2x.klarnact.klarna.model.order;

import com.e2x.klarnact.klarna.model.Address;
import com.e2x.klarnact.klarna.model.ShippingInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@ToString
@Builder(builderClassName = "CaptureBuilder", toBuilder = true)
@JsonDeserialize(builder = Capture.CaptureBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class Capture implements Transaction {

    @JsonProperty("capture_id")
    private final String id;
    private final String reference;
    private final String klarnaReference;
    private final Integer capturedAmount;
    private final ZonedDateTime capturedAt;
    private final String description;
    private final List<OrderLine> orderLines;
    private final Integer refundedAmount;
    private final Address billingAddress;
    private final Address shippingAddress;
    private final List<ShippingInfo> shippingInfo;
    private final Integer shippingDelay;

    @Override
    public TransactionType type() {
        return TransactionType.CAPTURE;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class CaptureBuilder {
    }
}
