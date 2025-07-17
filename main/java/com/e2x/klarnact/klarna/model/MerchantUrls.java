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
@Builder(builderClassName = "MerchantUrlsBuilder", toBuilder = true)
@JsonDeserialize(builder = MerchantUrls.MerchantUrlsBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class MerchantUrls {
    private final String confirmation;
    private final String notification;
    private final String push;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class MerchantUrlsBuilder {
    }
}
