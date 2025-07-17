package com.e2x.klarnact.klarna.model.payment;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Collection;

@Getter
@ToString
@Builder(builderClassName = "SessionCreatedBuilder", toBuilder = true)
@JsonDeserialize(builder = SessionCreated.SessionCreatedBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class SessionCreated {
    private final String clientToken;
    private final String sessionId;
    private final Collection<PaymentMethodCategory> paymentMethodCategories;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class SessionCreatedBuilder {
    }
}
