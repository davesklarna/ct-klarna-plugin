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
@Builder(builderClassName = "ProductIdentifiersBuilder", toBuilder = true)
@JsonDeserialize(builder = ProductIdentifiers.ProductIdentifiersBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class ProductIdentifiers {
    private final String brand;
    private final String categoryPath;
    private final String globalTradeItemNumber;
    private final String manufacturerPartNumber;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class ProductIdentifiersBuilder {
    }
}
