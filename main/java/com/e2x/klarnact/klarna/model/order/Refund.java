package com.e2x.klarnact.klarna.model.order;

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
@Builder(builderClassName = "RefundBuilder", toBuilder = true)
@JsonDeserialize(builder = Refund.RefundBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class Refund implements Transaction {

    @JsonProperty("Refund_id")
    private final String id;
    private final String reference;
    private final Integer refundedAmount;
    private final ZonedDateTime refundedAt;
    private final String description;
    private final List<OrderLine> orderLines;
    private final boolean creditInvoice;

    @Override
    public TransactionType type() {
        return TransactionType.REFUND;
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class RefundBuilder {
    }
}
