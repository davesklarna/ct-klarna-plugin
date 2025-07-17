package com.e2x.klarnact.order.create;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.cart.CartBuilder;
import com.commercetools.api.models.cart.CartReference;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderReference;
import com.commercetools.api.models.order.OrderSetCustomFieldAction;
import com.commercetools.api.models.order.OrderSetCustomTypeAction;
import com.commercetools.api.models.order.PaymentInfo;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.PaymentReference;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.FieldContainer;
import com.e2x.klarnact.commercetools.cart.CartService;
import com.e2x.klarnact.commercetools.config.CommerceToolsConfig;
import com.e2x.klarnact.commercetools.config.CommerceToolsMapperConfig;
import com.e2x.klarnact.commercetools.order.data.OrderTestData;
import com.e2x.klarnact.config.CommerceToolsMapperConfigTest;
import com.e2x.klarnact.exception.KlarnaCtException;
import com.e2x.klarnact.exception.NotFoundException;
import com.e2x.klarnact.klarna.client.KlarnaOrderService;
import com.e2x.klarnact.klarna.client.KlarnaPaymentService;
import com.e2x.klarnact.klarna.client.provider.KlarnaOrderServiceProvider;
import com.e2x.klarnact.klarna.client.provider.KlarnaPaymentServiceProvider;
import com.e2x.klarnact.klarna.model.order.OrderCreated;
import com.e2x.klarnact.mapper.OrderLineMapper;
import com.e2x.klarnact.order.OrderToOrderMapper;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.e2x.klarnact.CustomFields.Order.EXTRA_MERCHANT_DATA;
import static com.e2x.klarnact.CustomFields.Order.KLARNA_ORDER_ID;
import static com.e2x.klarnact.CustomFields.Payment.KLARNA_AUTH_TOKEN;
import static com.e2x.klarnact.PaymentProducer.getPayment;
import static com.e2x.klarnact.commercetools.cart.data.CartTestData.getCart;
import static com.e2x.klarnact.payment.PaymentHelper.getLatestKlarnaPayment;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    CartService cartService;

    @Mock
    KlarnaPaymentService klarnaPaymentService;


    @Mock
    KlarnaPaymentServiceProvider klarnaPaymentServiceProvider;

    @Mock
    KlarnaOrderService klarnaOrderService;

    @Mock
    KlarnaOrderServiceProvider klarnaOrderServiceProvider;

    @Mock
    CommerceToolsConfig commerceToolsConfigMock;

    OrderToOrderMapper orderToOrderMapper;

    CreateOrderService createOrderService;

    @BeforeEach
    public void setup() {
        lenient().when(klarnaOrderServiceProvider.get("NL")).thenReturn(klarnaOrderService);
        lenient().when(klarnaPaymentServiceProvider.get("NL")).thenReturn(klarnaPaymentService);

        final CommerceToolsMapperConfig commerceToolsMapperConfig = new CommerceToolsMapperConfigTest();
        final OrderLineMapper orderLineMapper = new OrderLineMapper(commerceToolsMapperConfig);
        orderToOrderMapper = new OrderToOrderMapper(orderLineMapper);

        createOrderService = new CreateOrderService(
                cartService,
                klarnaPaymentServiceProvider,
                klarnaOrderServiceProvider,
                orderToOrderMapper,
                commerceToolsConfigMock
        );


    }

    @Test
    void klarnaExceptionIfNotCreate() {
        final var ex = assertThrows(KlarnaCtException.class, () -> createOrderService.createOrder(
                new OrderRequest("update", OrderReference.of())
        ));

        assertEquals("Incorrect request type for endpoint", ex.getMessage());
    }

    @ParameterizedTest
    @MethodSource
    public void noActionsReturnedWhenNoCart(OrderRequest orderRequest) {

        final var res = createOrderService.createOrder(orderRequest)
                .await().atMost(Duration.of(1000, ChronoUnit.MILLIS));

        assertTrue(res.getActions().isEmpty());
    }

    private static Stream<OrderRequest> noActionsReturnedWhenNoCart() {
        return Stream.of(
                new OrderRequest(
                        "Create",
                        null
                ),
                new OrderRequest(
                        "Create",
                        OrderReference.builder()
                                .build()
                ),
                new OrderRequest(
                        "Create",
                        OrderReference.builder()
                                .obj(Order.builder().cart(null).build())
                                .build()
                ),
                new OrderRequest(
                        "Create",
                        OrderReference.builder()
                                .obj(Order.builder().cart(CartReference.builder().build()).build())
                                .build()
                )
        );
    }

    @Test
    public void setCustomFieldsWhenOrderHasACustomType() {
        final String klarnaAuthToken = UUID.randomUUID().toString();
        final String klarnaOrderId = UUID.randomUUID().toString();
        final String extraMerchantData = "{\"extra\":\"Extra Merchant Data\"}";
        final Order order = requireNonNull(OrderTestData.getOrder());

        order.setCustom(CustomFields.builder()
                .fields(FieldContainer.builder()
                        .values(Map.of(EXTRA_MERCHANT_DATA, extraMerchantData))
                        .build())
                .build()
        );
        final String cartId = order.getCart().getId();
        final var cor = new OrderRequest(
                "Create",
                OrderReference.builder()
                        .obj(order)
                        .build()
        );

        final Cart cart = CartBuilder.of(requireNonNull(getCart()))
                .paymentInfo(PaymentInfo.builder()
                        .payments(List.of(PaymentReference.builder()
                                .obj(Payment.builder(requireNonNull(getPayment()))
                                        .custom(CustomFields.builder()
                                                .fields(FieldContainer.builder()
                                                        .values(Map.of(
                                                                KLARNA_AUTH_TOKEN, klarnaAuthToken))
                                                        .build())
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();


        when(cartService.findCartById(cartId)).thenReturn(Uni.createFrom().item(() -> cart));
        when(klarnaPaymentService.createOrder(
                eq(klarnaAuthToken),
                any(com.e2x.klarnact.klarna.model.order.Order.class))
        ).thenReturn(Uni.createFrom().item(() -> OrderCreated.builder()
                .orderId(klarnaOrderId)
                .build()
        ));

        getLatestKlarnaPayment(cart.getPaymentInfo().getPayments()).map(PaymentReference::getObj)
                .ifPresent(p -> {
                            var klarnaOrder = orderToOrderMapper.mapToOrder(order, new CartAndPayment(cart, p));
                            var fetchedOrder = klarnaOrder.flatMap(o -> Uni.createFrom().item(o.toBuilder().attachment(null).build()));
                            when(klarnaOrderService.findOrder(klarnaOrderId))
                                    .thenReturn(fetchedOrder);
                        }
                );

        final var res = createOrderService.createOrder(cor)
                .await().atMost(Duration.of(1000, ChronoUnit.MILLIS));

        assertFalse(res.getActions().isEmpty());
        assertTrue(res.getActions().stream().anyMatch(a -> a instanceof OrderSetCustomFieldAction));
        assertTrue(res.getActions().stream()
                .filter(OrderSetCustomFieldAction.class::isInstance)
                .map(OrderSetCustomFieldAction.class::cast).anyMatch(a -> a.getName().equals(KLARNA_ORDER_ID)));
    }

    @Test
    public void setCustomTypeWhenOrderHasNoCustomFields() {
        final String klarnaAuthToken = UUID.randomUUID().toString();
        final String klarnaOrderId = UUID.randomUUID().toString();
        final Order order = requireNonNull(OrderTestData.getOrder());
        order.setCustom(null);

        final String cartId = order.getCart().getId();
        final var cor = new OrderRequest(
                "Create",
                OrderReference.builder()
                        .obj(order)
                        .build()
        );

        final Cart cart = CartBuilder.of(requireNonNull(getCart()))
                .paymentInfo(PaymentInfo.builder()
                        .payments(List.of(PaymentReference.builder()
                                .obj(Payment.builder(requireNonNull(getPayment()))
                                        .custom(CustomFields.builder()
                                                .fields(FieldContainer.builder()
                                                        .values(Map.of(
                                                                KLARNA_AUTH_TOKEN, klarnaAuthToken))
                                                        .build())
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();


        when(cartService.findCartById(cartId)).thenReturn(Uni.createFrom().item(() -> cart));
        when(klarnaPaymentService.createOrder(
                eq(klarnaAuthToken),
                any(com.e2x.klarnact.klarna.model.order.Order.class))
        ).thenReturn(Uni.createFrom().item(() -> OrderCreated.builder()
                .orderId(klarnaOrderId)
                .build()
        ));
        when(commerceToolsConfigMock.orderCustomType()).thenReturn("testOrderType");

        getLatestKlarnaPayment(cart.getPaymentInfo().getPayments()).map(PaymentReference::getObj)
                .ifPresent(p -> {
                            var klarnaOrder = orderToOrderMapper.mapToOrder(order, new CartAndPayment(cart, p));
                            var fetchedOrder = klarnaOrder.flatMap(o -> Uni.createFrom().item(o.toBuilder().attachment(null).build()));
                            when(klarnaOrderService.findOrder(klarnaOrderId))
                                    .thenReturn(fetchedOrder);
                        }
                );

        final var res = createOrderService.createOrder(cor)
                .await().atMost(Duration.of(1000, ChronoUnit.MILLIS));

        assertFalse(res.getActions().isEmpty());
        assertTrue(res.getActions().stream().anyMatch(a -> a instanceof OrderSetCustomTypeAction));
        assertTrue(res.getActions().stream()
                .filter(OrderSetCustomTypeAction.class::isInstance)
                .map(OrderSetCustomTypeAction.class::cast)
                .anyMatch(a -> a.getType().getKey().equals(commerceToolsConfigMock.orderCustomType())
                        && a.getFields().values().containsKey(KLARNA_ORDER_ID)));
    }

    @Test
    public void notFoundExceptionWhenCartNotFound() {
        final Order order = requireNonNull(OrderTestData.getOrder());
        final String cartId = order.getCart().getId();
        final var cor = new OrderRequest(
                "Create",
                OrderReference.builder()
                        .obj(order)
                        .build()
        );

        when(cartService.findCartById(cartId)).thenReturn(Uni.createFrom()
                .failure(() -> new NotFoundException(format("Cart Not Found with id of %s", cartId))));

        final var nfe = assertThrows(NotFoundException.class,
                () -> createOrderService.createOrder(cor).await()
                        .atMost(Duration.of(1000, ChronoUnit.MILLIS)));

        assertEquals(NOT_FOUND, nfe.getStatus());
        assertEquals(format("Cart Not Found with id of %s", cartId), nfe.getMessage());
    }

    @ParameterizedTest
    @MethodSource
    public void klarnaExceptionWhenNoPaymentForCart(Cart cart) {
        final Order order = requireNonNull(OrderTestData.getOrder());
        final String cartId = order.getCart().getId();
        final var cor = new OrderRequest(
                "Create",
                OrderReference.builder()
                        .obj(order)
                        .build()
        );

        when(cartService.findCartById(cartId)).thenReturn(
                Uni.createFrom().item(cart)
        );

        final var res = createOrderService.createOrder(cor).await()
                .atMost(Duration.of(1000, ChronoUnit.MILLIS));

        assertNotNull(res);
        assertTrue(res.getActions().isEmpty());
    }

    private static Stream<Cart> klarnaExceptionWhenNoPaymentForCart() {
        final Cart cart = requireNonNull(getCart());
        return Stream.of(
                Cart.builder(cart)
                        .paymentInfo(null)
                        .build(),
                Cart.builder(cart)
                        .paymentInfo(PaymentInfo.builder()
                                .build())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource
    public void noActionsWhenCartRetrievedWithoutKlarnaPayments(Cart cart) {
        final Order order = requireNonNull(OrderTestData.getOrder());
        final String cartId = order.getCart().getId();
        final var cor = new OrderRequest(
                "Create",
                OrderReference.builder()
                        .obj(order)
                        .build()
        );

        when(cartService.findCartById(cartId)).thenReturn(Uni.createFrom().item(() -> cart));
        final var res = createOrderService.createOrder(cor)
                .await().atMost(Duration.of(1000, ChronoUnit.MILLIS));

        assertTrue(res.getActions().isEmpty());
    }

    private static Stream<Cart> noActionsWhenCartRetrievedWithoutKlarnaPayments() {
        return Stream.of(
                CartBuilder.of(requireNonNull(getCart()))
                        .paymentInfo(PaymentInfo.builder()
                                .payments(List.of(PaymentReference.builder()
                                        .obj(Payment.builder()
                                                .custom(CustomFields.builder()
                                                        .fields(FieldContainer.builder()
                                                                .values(Map.of())
                                                                .build())
                                                        .build())
                                                .build())
                                        .build()))
                                .build())
                        .build(),

                CartBuilder.of(getCart())
                        .paymentInfo(PaymentInfo.builder()
                                .payments(List.of(PaymentReference.builder()
                                        .obj(Payment.builder()
                                                .custom(CustomFields.builder()
                                                        .fields(FieldContainer.builder()
                                                                .build())
                                                        .build())
                                                .build())
                                        .build()))
                                .build())
                        .build(),

                CartBuilder.of(getCart())
                        .paymentInfo(PaymentInfo.builder()
                                .payments(List.of(PaymentReference.builder()
                                        .obj(Payment.builder()
                                                .custom(CustomFields.builder()
                                                        .build())
                                                .build())
                                        .build()))
                                .build())
                        .build(),

                CartBuilder.of(getCart())
                        .paymentInfo(PaymentInfo.builder()
                                .payments(List.of(PaymentReference.builder()
                                        .obj(Payment.builder()
                                                .build())
                                        .build()))
                                .build())
                        .build(),

                CartBuilder.of(getCart())
                        .paymentInfo(PaymentInfo.builder()
                                .payments(List.of(PaymentReference.builder()
                                        .build()))
                                .build())
                        .build(),

                CartBuilder.of(getCart())
                        .paymentInfo(PaymentInfo.builder()
                                .payments(List.of())
                                .build())
                        .build()
        );
    }
}
