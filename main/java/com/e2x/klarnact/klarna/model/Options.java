package com.e2x.klarnact.klarna.model;

import com.e2x.klarnact.klarna.model.customer.CustomerType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
@Builder(builderClassName = "OptionsBuilder", toBuilder = true)
@JsonDeserialize(builder = Options.OptionsBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class Options {
    private final boolean requireValidateCallbackSuccess;
    private final AcquiringChannel acquiringChannel;
    private final boolean vatRemoved;
    private final boolean allowSeparateShippingAddress;
    private final HexColour colorButton;
    private final HexColour colorButtonText;
    private final HexColour colorCheckBox;
    private final HexColour colorCheckBoxCheckmark;
    private final HexColour colorHeader;
    private final HexColour colorLink;
    private final boolean dateOfBirthMandatory;
    private final String shippingDetails;
    private final String additionalMerchantTerms;
    private final boolean phoneMandatory;
    private final String radiusBorder;
    private final List<CustomerType> allowedCustomerTypes;
    private final boolean showSubtotalDetail;
    private final boolean nationalIdentificationNumberMandatory;
    private final boolean verifyNationalIdentificationNumber;
    private final boolean showVatRegistrationNumberField;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class OptionsBuilder {
    }
}
