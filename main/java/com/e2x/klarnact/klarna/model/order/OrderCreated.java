package com.e2x.klarnact.klarna.model.order;

import com.e2x.klarnact.klarna.model.payment.FraudStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Builder(builderClassName = "OrderCreatedBuilder", toBuilder = true)
@JsonDeserialize(builder = OrderCreated.OrderCreatedBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class OrderCreated {

    private final String orderId;
    @JsonProperty("authorized_payment_method")
    private final AuthorisedPaymentMethod authorisedPaymentMethod;
    private final FraudStatus fraudStatus;
    private final String redirectUrl;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class OrderCreatedBuilder {
    }
}
