package com.e2x.klarnact.klarna.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderClassName = "AddressBuilder", toBuilder = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonDeserialize(builder = Address.AddressBuilder.class)
@RegisterForReflection
public class Address {
    private final String givenName;
    private final String familyName;
    @JsonProperty("organization_name")
    private final String organisationName;
    private final String email;
    private final String phone;
    // title should use Title - but api returns empty string so needs mapping
    private final String title;
    private final String streetAddress;
    private final String streetAddress2;
    private final String postalCode;
    private final String city;
    private final String region;
    private final String country;
    private final String careOf;
    private final String reference;
    private final String attention;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class AddressBuilder {
    }
}
