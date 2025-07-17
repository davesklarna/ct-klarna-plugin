package com.e2x.klarnact.klarna.model.customer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
@Builder(builderClassName = "CustomerBuilder", toBuilder = true)
@JsonDeserialize(builder = Customer.CustomerBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class Customer {
    private final CustomerType type;
    private final CustomerGender gender;
    private final LocalDate dateOfBirth;
    @JsonProperty("organization_registration_id")
    private final String organisationId;
    private final String vatId;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class CustomerBuilder{
    }
}
