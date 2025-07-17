package com.e2x.klarnact.klarna.model.payment;

import com.e2x.klarnact.klarna.model.AssetUrls;
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
@Builder(builderClassName = "PaymentMethodCategoryBuilder", toBuilder = true)
@JsonDeserialize(builder = PaymentMethodCategory.PaymentMethodCategoryBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class PaymentMethodCategory {
    private final PaymentCategoryIdentifier paymentCategoryIdentifier;
    private final String name;
    private final AssetUrls assetUrls;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class PaymentMethodCategoryBuilder {
    }
}
