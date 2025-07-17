package com.e2x.klarnact.klarna.model.order;

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
@Builder(builderClassName = "AuthorisedPaymentMethodBuilder", toBuilder = true)
@JsonDeserialize(builder = AuthorisedPaymentMethod.AuthorisedPaymentMethodBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class AuthorisedPaymentMethod {

    private final AuthorisedPaymentMethodType type;
    private final Integer days;
    private final Integer numberOfInstallments;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class AuthorisedPaymentMethodBuilder {
    }
}
