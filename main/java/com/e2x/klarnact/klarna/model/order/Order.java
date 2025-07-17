package com.e2x.klarnact.klarna.model.order;

import com.e2x.klarnact.klarna.model.*;
import com.e2x.klarnact.klarna.model.customer.Customer;
import com.e2x.klarnact.klarna.model.payment.PaymentProvider;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@ToString
@Builder(builderClassName = "OrderBuilder", toBuilder = true)
@JsonDeserialize(builder = Order.OrderBuilder.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@RegisterForReflection
public class Order {
    private final String orderId;
    private final String name;
    private final String status;
    private final String locale;
    private final Customer customer;
    private final Options options;
    private final Attachment attachment;
    private final boolean recurring;
    private final List<String> tags;
    private final String purchaseCountry;
    private final KlarnaCurrency purchaseCurrency;
    private final Integer orderAmount;
    private final Integer orderTaxAmount;
    private final List<OrderLine> orderLines;
    private final String htmlSnippet;
    private final MerchantUrls merchantUrls;
    private final String merchantReference1;
    private final String merchantReference2;
    private final LocalDateTime startedAt;
    private final LocalDateTime completedAt;
    private final LocalDateTime lastModifiedAt;
    private final List<PaymentProvider> externalPaymentMethods;
    private final List<PaymentProvider> externalCheckout;
    private final List<String> shippingCountries;
    private final List<ShippingOption> shippingOptions;
    private final String merchantData;
    private final ShippingOption selectedShippingOption;
    private final List<String> billingCountries;
    private final String recurringToken;
    private final String recurringDescription;
    private final Address billingAddress;
    private final Address shippingAddress;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @RegisterForReflection
    public static class OrderBuilder {
    }
}
