package com.e2x.klarnact.klarna.model.payment;

import com.e2x.klarnact.klarna.model.*;
import com.e2x.klarnact.klarna.model.customer.Customer;
import com.e2x.klarnact.klarna.model.order.OrderLine;
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
import java.util.Collections;
import java.util.List;

@Getter
@ToString
@Builder(builderClassName = "CreditSessionBuilder", toBuilder = true)
@JsonDeserialize(builder = CreditSession.CreditSessionBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class CreditSession {
    private final String locale;
    private final String purchaseCountry;
    private final KlarnaCurrency purchaseCurrency;
    private final Integer orderAmount;
    private final List<OrderLine> orderLines;
    private final MerchantUrls merchantUrls;
    private final Attachment attachment;
    private final Address billingAddress;
    private final Address shippingAddress;
    private final List<String> customPaymentMethodIds;
    private final Customer customer;
    private final String design;
    private final String merchantData;
    private final String merchantReference1;
    private final String merchantReference2;
    private final Options options;
    private final Integer orderTaxAmount;
    @JsonProperty("authorization_token")
    private final String authorisationToken;
    private final String clientToken;
    private final ZonedDateTime expiresAt;
    private final List<PaymentMethodCategory> paymentMethodCategories;
    private final Status status;
    private final String id;

    public List<OrderLine> getOrderLines() {
        return orderLines == null ? List.of() : Collections.unmodifiableList(orderLines);
    }

    public List<String> getCustomPaymentMethodIds() {
        return customPaymentMethodIds == null ? List.of() : Collections.unmodifiableList(customPaymentMethodIds);
    }

    public List<PaymentMethodCategory> getPaymentMethodCategories() {
        return paymentMethodCategories == null ? List.of() : Collections.unmodifiableList(paymentMethodCategories);
    }

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class CreditSessionBuilder {
    }
}
