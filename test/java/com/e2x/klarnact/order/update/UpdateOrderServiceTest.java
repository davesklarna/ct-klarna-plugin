package com.e2x.klarnact.order.update;

import com.commercetools.api.models.order.*;
import com.commercetools.api.models.payment.*;
import com.commercetools.api.models.type.CustomFields;
import com.e2x.klarnact.commercetools.order.OrderService;
import com.e2x.klarnact.exception.KlarnaCtException;
import com.e2x.klarnact.klarna.client.KlarnaOrderService;
import com.e2x.klarnact.klarna.client.provider.KlarnaOrderServiceProvider;
import com.e2x.klarnact.order.create.OrderRequest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static com.e2x.klarnact.CustomFields.Order.KLARNA_ORDER_ID;
import static com.e2x.klarnact.PaymentProducer.getPayment;
import static com.e2x.klarnact.commercetools.order.data.OrderTestData.getOrder;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOrderServiceTest {

    @Mock
    OrderService orderService;

    @Mock
    KlarnaOrderService klarnaOrderService;

    @Mock
    KlarnaOrderServiceProvider klarnaOrderServiceProvider;

    UpdateOrderService updateOrderService;

    @BeforeEach
    void setUp() {
        lenient().when(klarnaOrderServiceProvider.get(anyString())).thenReturn(klarnaOrderService);
        updateOrderService = new UpdateOrderService(orderService, klarnaOrderServiceProvider);
    }

    @Test
    void klarnaExceptionIfNotUpdate() {
        final var ex = assertThrows(KlarnaCtException.class, () -> updateOrderService.updateOrder(
                new OrderRequest("create", OrderReference.of())
        ));

        assertEquals("Incorrect request type for endpoint", ex.getMessage());
    }

    @Test
    void ignoreWithoutKlarnaOrderId() {
        final Order order = requireNonNull(getOrder());
        order.setCustom(CustomFields.of());
        final var orderReference = OrderReference.builder()
                .id(order.getId())
                .obj(order)
                .build();

        final var res = updateOrderService.updateOrder(new OrderRequest("update", orderReference))
                .await().atMost(Duration.of(1000, ChronoUnit.MILLIS));

        assertNotNull(res);
        assertNotNull(res.getActions());
        assertTrue(res.getActions().isEmpty());
    }

    @Test
    void cancelOrderSuccess() {
        final Order order = requireNonNull(getOrder());
        final String klarnaOrderId = (String) order.getCustom().getFields().values().get(KLARNA_ORDER_ID);
        final var orderReference = OrderReference.builder()
                .id(order.getId())
                .obj(order)
                .build();

        when(orderService.findOrderById(order.getId()))
                .thenReturn(Uni.createFrom().item(() -> OrderBuilder.of(order)
                        .orderState(OrderState.OPEN)
                        .build()
                ));

        when(klarnaOrderService.cancelOrder(klarnaOrderId)).thenReturn(
                Uni.createFrom().item(Response.noContent().build())
        );

        order.setOrderState(OrderState.CANCELLED);

        final var res = updateOrderService.updateOrder(new OrderRequest("update", orderReference))
                .await().atMost(Duration.of(1000, ChronoUnit.MILLIS));

        assertNotNull(res);
        assertNotNull(res.getActions());
        assertTrue(res.getActions().isEmpty());
    }

    @Test
    void unableToCancelKlarnaOrder() {
        final Order order = requireNonNull(getOrder());
        final String klarnaOrderId = (String) order.getCustom().getFields().values().get(KLARNA_ORDER_ID);
        final var orderReference = OrderReference.builder()
                .id(order.getId())
                .obj(order)
                .build();

        when(orderService.findOrderById(order.getId()))
                .thenReturn(Uni.createFrom().item(() -> OrderBuilder.of(order)
                        .orderState(OrderState.OPEN)
                        .build()
                ));

        when(klarnaOrderService.cancelOrder(klarnaOrderId)).thenReturn(
                Uni.createFrom().item(Response.status(400).build())
        );

        order.setOrderState(OrderState.CANCELLED);

        final var exc = assertThrows(KlarnaCtException.class, () ->
                updateOrderService.updateOrder(new OrderRequest("update", orderReference))
                        .await().atMost(Duration.of(1000, ChronoUnit.MILLIS)));

        assertNotNull(exc);
        assertEquals(Response.Status.BAD_REQUEST, exc.getStatus());
        assertEquals(format("Unable to cancel order %s:%s", order.getId(), klarnaOrderId), exc.getMessage());
    }

    @Test
    void orderAlreadyCancelled() {
        final Order order = requireNonNull(getOrder());
        order.setOrderState(OrderState.CANCELLED);
        final var orderReference = OrderReference.builder()
                .id(order.getId())
                .obj(order)
                .build();

        when(orderService.findOrderById(order.getId()))
                .thenReturn(Uni.createFrom().item(() -> OrderBuilder.of(order)
                        .build()
                ));

        final var res = updateOrderService.updateOrder(new OrderRequest("update", orderReference))
                .await().atMost(Duration.of(1000, ChronoUnit.MILLIS));

        assertNotNull(res);
        assertNotNull(res.getActions());
        assertTrue(res.getActions().isEmpty());
    }

    @Test
    void orderHasCaptureCannotBeCancelled() {
        final Order order = requireNonNull(getOrder());
        order.setOrderState(OrderState.CANCELLED);
        final var orderReference = OrderReference.builder()
                .id(order.getId())
                .obj(order)
                .build();
        final Payment payment = requireNonNull(getPayment());
        payment.getTransactions().add(Transaction.builder()
                .id(UUID.randomUUID().toString())
                .type(TransactionType.CHARGE)
                .state(TransactionState.SUCCESS)
                .build()
        );

        when(orderService.findOrderById(order.getId()))
                .thenReturn(Uni.createFrom().item(() -> OrderBuilder.of(order)
                        .orderState(OrderState.CONFIRMED)
                        .paymentInfo(PaymentInfo.builder()
                                .payments(List.of(
                                        PaymentReference.builder()
                                                .id(payment.getId())
                                                .obj(payment)
                                                .build()
                                )).build())
                        .build()
                ));


        final var exc = assertThrows(KlarnaCtException.class, () ->
                updateOrderService.updateOrder(new OrderRequest("update", orderReference))
                        .await().atMost(Duration.of(1000, ChronoUnit.MILLIS)));

        assertNotNull(exc);
        assertEquals(Response.Status.BAD_REQUEST, exc.getStatus());
        assertEquals(format(
                "Unable to cancel order %s as captures have been applied. Please use refund functionality.",
                order.getId()
        ), exc.getMessage());
    }

    @Test
    void otherOrderUpdatesPassThrough() {
        final Order order = requireNonNull(getOrder());
        order.setOrderState(OrderState.OPEN);
        final var orderReference = OrderReference.builder()
                .id(order.getId())
                .obj(order)
                .build();

        when(orderService.findOrderById(order.getId()))
                .thenReturn(Uni.createFrom().item(() -> OrderBuilder.of(order)
                        .build()
                ));

        final var res = updateOrderService.updateOrder(new OrderRequest("update", orderReference))
                .await().atMost(Duration.of(1000, ChronoUnit.MILLIS));

        assertNotNull(res);
        assertNotNull(res.getActions());
        assertTrue(res.getActions().isEmpty());
    }
}
