package com.e2x.klarnact.order.create;

import com.commercetools.api.models.common.Address;
import com.commercetools.api.models.order.OrderSetBillingAddressAction;
import com.commercetools.api.models.order.OrderSetCustomFieldAction;
import com.commercetools.api.models.order.OrderSetCustomTypeAction;
import com.commercetools.api.models.order.OrderUpdate;
import com.commercetools.api.models.order.OrderUpdateAction;
import com.commercetools.api.models.payment.PaymentReference;
import com.commercetools.api.models.type.FieldContainer;
import com.commercetools.api.models.type.TypeResourceIdentifier;
import com.e2x.klarnact.commercetools.cart.CartService;
import com.e2x.klarnact.commercetools.config.CommerceToolsConfig;
import com.e2x.klarnact.exception.KlarnaCtException;
import com.e2x.klarnact.klarna.client.provider.KlarnaOrderServiceProvider;
import com.e2x.klarnact.klarna.client.provider.KlarnaPaymentServiceProvider;
import com.e2x.klarnact.klarna.model.order.Order;
import com.e2x.klarnact.order.OrderToOrderMapper;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.e2x.klarnact.CustomFields.Order.EXTRA_MERCHANT_DATA;
import static com.e2x.klarnact.CustomFields.Order.KLARNA_ORDER_ID;
import static com.e2x.klarnact.mapper.AddressMapper.map;
import static com.e2x.klarnact.order.OrderHelper.EMPTY;
import static com.e2x.klarnact.payment.PaymentHelper.getAuthToken;
import static com.e2x.klarnact.payment.PaymentHelper.getLatestKlarnaPayment;
import static java.lang.String.format;

@Slf4j
@ApplicationScoped
@AllArgsConstructor
public class CreateOrderService {

    private final CartService cartService;
    private final KlarnaPaymentServiceProvider paymentServiceProvider;
    private final KlarnaOrderServiceProvider orderServiceProvider;
    private final OrderToOrderMapper orderToOrderMapper;
    private final CommerceToolsConfig commerceToolsConfig;

    public Uni<OrderUpdate> createOrder(OrderRequest createOrderRequest) {

        if (!"create".equalsIgnoreCase(createOrderRequest.getAction())) {
            throw new KlarnaCtException("Incorrect request type for endpoint");
        }

        if (createOrderRequest.getResource() == null
                || createOrderRequest.getResource().getObj() == null
                || createOrderRequest.getResource().getObj().getCart() == null
                || createOrderRequest.getResource().getObj().getCart().getId() == null
        ) {
            /* Unable to read from cart, thus no way of getting existing payments
            for the auth token since the order would not have been persisted yet.
            This may be a legitimate business case so cannot throw exception just return no updates.
            */
            return Uni.createFrom().item(EMPTY);
        }

        final String cartId = createOrderRequest.getResource().getObj().getCart().getId();
        final com.commercetools.api.models.order.Order orderIn = createOrderRequest.getResource().getObj();
        return cartService.findCartById(cartId)
                .map(cart -> {
                    if (cart.getPaymentInfo() == null) {
                        return Optional.<CartAndPayment>empty();
                    }
                    return getLatestKlarnaPayment(cart.getPaymentInfo().getPayments())
                            .map(PaymentReference::getObj)
                            .map(p -> new CartAndPayment(cart, p));
                })
                .flatMap(cartAndPayment -> {
                    if (cartAndPayment.isEmpty()) {
                        log.debug(format("No authorisation token found for cart %s", cartId));
                        return Uni.createFrom().item(EMPTY);
                    } else {
                        return orderToOrderMapper.mapToOrder(createOrderRequest.getResource().getObj(), cartAndPayment.get())
                                .flatMap(order -> paymentServiceProvider.get(cartAndPayment.get().getCart().getCountry()).createOrder(getAuthToken(
                                                cartAndPayment.get().getPayment()), order)
                                        .flatMap(created -> orderServiceProvider.get(cartAndPayment.get().getCart().getCountry()).findOrder(created.getOrderId())
                                                .map(createdOrder -> createdOrder.toBuilder()
                                                        .attachment(order.getAttachment()).build()))
                                ).map(createdOrder -> OrderUpdate.builder()
                                        .actions(actions(createdOrder, orderIn))
                                        .version(orderIn.getVersion())
                                        .build()
                                );
                    }
                });
    }

    private List<OrderUpdateAction> actions(Order order, com.commercetools.api.models.order.Order orderIn) {
        final List<OrderUpdateAction> actions = new ArrayList<>();
        if (orderIn.getCustom() == null) {
            log.debug("Adding type {}", commerceToolsConfig.orderCustomType());
            actions.add(OrderSetCustomTypeAction.builder()
                    .type(TypeResourceIdentifier.builder()
                            .key(commerceToolsConfig.orderCustomType())
                            .build())
                    .fields(FieldContainer.builder()
                            .values(customFields(order))
                            .build()
                    ).build());
        } else {
            log.debug("Adding customFields to existing type");
            customFields(order).forEach((key, value) ->
                    actions.add(OrderSetCustomFieldAction.builder()
                            .name(key)
                            .value(value)
                            .build()));
        }

        changedAddress(order, orderIn).ifPresent(address -> actions.add(
                OrderSetBillingAddressAction.builder()
                        .address(address)
                        .build())
        );

        return actions;
    }

    private Map<String, Object> customFields(Order orderCreated) {
        final Map<String, Object> customFields = new HashMap<>();
        customFields.put(KLARNA_ORDER_ID, orderCreated.getOrderId());
        if (orderCreated.getAttachment() != null && orderCreated.getAttachment().getBody() != null) {
            customFields.put(EXTRA_MERCHANT_DATA, orderCreated.getAttachment().getBody());
        }
        return customFields;
    }

    private Optional<Address> changedAddress(Order order, com.commercetools.api.models.order.Order orderIn) {
        final var updatedAddress = map(order.getBillingAddress());
        final var originalAddress = orderIn.getBillingAddress();
        return updatedAddress == null || updatedAddress.equals(originalAddress) ? Optional.empty() : Optional.of(updatedAddress);
    }
}
