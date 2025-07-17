package com.e2x.klarnact.payment.update;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.*;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.FieldContainer;
import com.e2x.klarnact.commercetools.cart.CartService;
import com.e2x.klarnact.commercetools.config.CommerceToolsMapperConfig;
import com.e2x.klarnact.commercetools.order.OrderService;
import com.e2x.klarnact.config.CommerceToolsMapperConfigTest;
import com.e2x.klarnact.exception.KlarnaCtException;
import com.e2x.klarnact.klarna.client.KlarnaCaptureService;
import com.e2x.klarnact.klarna.client.KlarnaOrderService;
import com.e2x.klarnact.klarna.client.KlarnaPaymentService;
import com.e2x.klarnact.klarna.client.KlarnaRefundService;
import com.e2x.klarnact.klarna.client.provider.KlarnaCaptureServiceProvider;
import com.e2x.klarnact.klarna.client.provider.KlarnaOrderServiceProvider;
import com.e2x.klarnact.klarna.client.provider.KlarnaPaymentServiceProvider;
import com.e2x.klarnact.klarna.client.provider.KlarnaRefundServiceProvider;
import com.e2x.klarnact.klarna.model.order.Capture;
import com.e2x.klarnact.klarna.model.order.Refund;
import com.e2x.klarnact.klarna.model.payment.CreditSession;
import com.e2x.klarnact.mapper.OrderLineMapper;
import com.e2x.klarnact.payment.PaymentRequest;
import com.e2x.klarnact.payment.create.CartToSessionMapper;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Stream;

import static com.commercetools.api.models.payment.PaymentChangeTransactionStateAction.CHANGE_TRANSACTION_STATE;
import static com.commercetools.api.models.payment.PaymentSetCustomFieldAction.SET_CUSTOM_FIELD;
import static com.e2x.klarnact.CustomFields.Order.KLARNA_ORDER_ID;
import static com.e2x.klarnact.CustomFields.Payment.*;
import static com.e2x.klarnact.PaymentProducer.getPayment;
import static com.e2x.klarnact.commercetools.cart.data.CartTestData.getCart;
import static com.e2x.klarnact.commercetools.order.data.OrderTestData.getOrder;
import static com.e2x.klarnact.klarna.HeaderId.CAPTURE_ID;
import static com.e2x.klarnact.klarna.HeaderId.REFUND_ID;
import static com.e2x.klarnact.payment.PaymentHelper.getAuthToken;
import static com.e2x.klarnact.payment.PaymentHelper.getOrderId;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePaymentServiceTest {

    @Mock
    CartService cartService;

    @Mock
    OrderService orderService;

    @Mock
    KlarnaCaptureService klarnaCaptureService;

    @Mock
    KlarnaCaptureServiceProvider klarnaCaptureServiceProvider;

    @Mock
    KlarnaOrderService klarnaOrderService;

    @Mock
    KlarnaOrderServiceProvider klarnaOrderServiceProvider;

    @Mock
    KlarnaPaymentService klarnaPaymentService;

    @Mock
    KlarnaPaymentServiceProvider klarnaPaymentServiceProvider;

    @Mock
    KlarnaRefundService klarnaRefundService;

    @Mock
    KlarnaRefundServiceProvider klarnaRefundServiceProvider;

    @Mock
    Response response;

    UpdatePaymentService updatePaymentService;

    @BeforeEach
    public void setup() {
        lenient().when(klarnaCaptureServiceProvider.get("NL")).thenReturn(klarnaCaptureService);
        lenient().when(klarnaOrderServiceProvider.get("NL")).thenReturn(klarnaOrderService);
        lenient().when(klarnaRefundServiceProvider.get("NL")).thenReturn(klarnaRefundService);

        final CommerceToolsMapperConfig commerceToolsMapperConfig = new CommerceToolsMapperConfigTest();
        final OrderLineMapper orderLineMapper = new OrderLineMapper(commerceToolsMapperConfig);
        final CartToSessionMapper cartToSessionMapper = new CartToSessionMapper(orderLineMapper);
        final CaptureMapper captureMapper = new CaptureMapper(orderLineMapper);
        final RefundMapper refundMapper = new RefundMapper(orderLineMapper);

        updatePaymentService = new UpdatePaymentService(
                cartService,
                orderService,
                klarnaCaptureServiceProvider,
                klarnaOrderServiceProvider,
                klarnaPaymentServiceProvider,
                klarnaRefundServiceProvider,
                cartToSessionMapper,
                captureMapper,
                refundMapper
        );
    }

    @Test
    public void exceptionThrownWhenNoPaymentReference() {
        final KlarnaCtException ex1 = assertThrows(KlarnaCtException.class,
                () -> updatePaymentService.updatePayment(new PaymentRequest(
                        "Update",
                        null
                )).await().indefinitely());

        assertEquals(Response.Status.BAD_REQUEST, ex1.getStatus());
        assertEquals("Payment Reference must not be null", ex1.getMessage());

        final PaymentReference paymentReference = PaymentReference.builder()
                .build();

        final KlarnaCtException ex2 = assertThrows(KlarnaCtException.class,
                () -> updatePaymentService.updatePayment(new PaymentRequest(
                        "Update",
                        paymentReference
                )).await().indefinitely());

        assertEquals(Response.Status.BAD_REQUEST, ex2.getStatus());
        assertEquals("Payment Reference must not be null", ex2.getMessage());
    }

    @Test
    public void klarnaExceptionIfNotAPaymentUpdate() {
        final var ex = assertThrows(KlarnaCtException.class,
                () -> updatePaymentService.updatePayment(new PaymentRequest(
                        "Create",
                        PaymentReference.of()
                )).await().indefinitely());

        assertEquals("Incorrect request type for endpoint", ex.getMessage());
    }

    @Test
    public void successfullyReturnsChangeCaptureState() {
        final Payment payment = requireNonNull(getPayment());
        final String sessionId = UUID.randomUUID().toString();
        payment.getCustom().getFields().values().put(SESSION_ID, sessionId);
        final String captureId = UUID.randomUUID().toString();
        final String actionAction = "changeTransactionState";
        final String transactionId = payment.getTransactions().get(0).getId();

        final PaymentReference paymentReference = PaymentReference.builder()
                .obj(payment)
                .build();

        final Order order = requireNonNull(getOrder());
        when(orderService.findOrderById((String) payment.getCustom().getFields().values().get(ORDER_ID)))
                .thenReturn(Uni.createFrom().item(order));

        when(klarnaCaptureService.createCapture(
                eq((String) order.getCustom().getFields().values().get(KLARNA_ORDER_ID)),
                any(Capture.class)
        )).thenReturn(Uni.createFrom().item(response));

        when(response.getHeaderString(CAPTURE_ID)).thenReturn(captureId);

        final var res = updatePaymentService.updatePayment(new PaymentRequest(
                "Update",
                paymentReference
        )).await().indefinitely();

        assertFalse(res.getActions().isEmpty());
        assertTrue(res.getActions().stream().anyMatch(a -> {
            if (!(a instanceof PaymentChangeTransactionStateAction)) return false;
            final var cts = (PaymentChangeTransactionStateAction) a;
            return actionAction.equals(cts.getAction())
                    && transactionId.equals(cts.getTransactionId())
                    && TransactionState.SUCCESS == cts.getState();
        }));
    }

    @Test
    public void successfullyReturnsChangeRefundState() {
        final Payment payment = requireNonNull(getPayment());
        final String sessionId = UUID.randomUUID().toString();
        payment.getCustom().getFields().values().put(SESSION_ID, sessionId);
        final String refundId = UUID.randomUUID().toString();
        final String actionAction = "changeTransactionState";
        final Transaction refund = Transaction.builder(payment.getTransactions().get(0))
                .type(TransactionType.REFUND)
                .build();
        final String transactionId = refund.getId();

        final PaymentReference paymentReference = PaymentReference.builder()
                .obj(Payment.builder(payment)
                        .transactions(List.of(refund))
                        .build())
                .build();

        final Order order = requireNonNull(getOrder());
        when(orderService.findOrderById((String) payment.getCustom().getFields().values().get(ORDER_ID)))
                .thenReturn(Uni.createFrom().item(order));

        when(klarnaRefundService.createRefund(
                eq((String) order.getCustom().getFields().values().get(KLARNA_ORDER_ID)),
                any(Refund.class)
        )).thenReturn(Uni.createFrom().item(response));

        when(response.getHeaderString(REFUND_ID)).thenReturn(refundId);

        final var res = updatePaymentService.updatePayment(new PaymentRequest(
                "Update",
                paymentReference
        )).await().indefinitely();

        assertFalse(res.getActions().isEmpty());
        assertTrue(res.getActions().stream().anyMatch(a -> {
            if (!(a instanceof PaymentChangeTransactionStateAction)) return false;
            final var cts = (PaymentChangeTransactionStateAction) a;
            return actionAction.equals(cts.getAction())
                    && transactionId.equals(cts.getTransactionId())
                    && TransactionState.SUCCESS == cts.getState();
        }));
    }

    @Test
    public void returnsTransactionFailedWhenNoCaptureId() {
        final Payment payment = requireNonNull(getPayment());
        final String sessionId = UUID.randomUUID().toString();
        payment.getCustom().getFields().values().put(SESSION_ID, sessionId);
        final String actionAction = "changeTransactionState";
        final String transactionId = payment.getTransactions().get(0).getId();

        final PaymentReference paymentReference = PaymentReference.builder()
                .obj(payment)
                .build();

        final Order order = requireNonNull(getOrder());
        when(orderService.findOrderById((String) payment.getCustom().getFields().values().get(ORDER_ID)))
                .thenReturn(Uni.createFrom().item(order));

        when(klarnaCaptureService.createCapture(
                eq((String) order.getCustom().getFields().values().get(KLARNA_ORDER_ID)),
                any(Capture.class)
        )).thenReturn(Uni.createFrom().item(response));

        when(response.getHeaderString(CAPTURE_ID)).thenReturn(null);

        final var res = updatePaymentService.updatePayment(new PaymentRequest(
                "Update",
                paymentReference
        )).await().indefinitely();

        assertFalse(res.getActions().isEmpty());
        assertTrue(res.getActions().stream().anyMatch(a -> {
            if (!(a instanceof PaymentChangeTransactionStateAction)) return false;
            final var cts = (PaymentChangeTransactionStateAction) a;
            return actionAction.equals(cts.getAction())
                    && transactionId.equals(cts.getTransactionId())
                    && TransactionState.FAILURE == cts.getState();
        }));
    }

    @ParameterizedTest
    @MethodSource
    public void successfullyIgnoresPaymentsNotOfInterest(Payment payment) {
        final var req = new PaymentRequest("Update", PaymentReference.builder()
                .id(payment.getId())
                .obj(payment)
                .build()
        );

        final var res = updatePaymentService.updatePayment(req)
                .await().indefinitely();

        assertTrue(res.getActions().isEmpty());
    }

    private static Stream<Payment> successfullyIgnoresPaymentsNotOfInterest() {
        final Payment payment = requireNonNull(getPayment());
        payment.setPaymentMethodInfo(null);
        return Stream.of(
                Payment.builder(payment)
                        .build(),
                Payment.builder(payment)
                        .paymentMethodInfo(PaymentMethodInfo.builder()
                                .build())
                        .build(),
                Payment.builder(payment)
                        .paymentMethodInfo(PaymentMethodInfo.builder()
                                .paymentInterface(UUID.randomUUID().toString())
                                .build())
                        .build(),
                Payment.builder(payment)
                        .custom(null)
                        .build(),
                Payment.builder(payment)
                        .custom(CustomFields.builder()
                                .build())
                        .build(),
                Payment.builder(payment)
                        .custom(CustomFields.builder()
                                .fields(FieldContainer.builder()
                                        .build())
                                .build())
                        .build(),
                Payment.builder(payment)
                        .custom(CustomFields.builder()
                                .fields(FieldContainer.builder()
                                        .values(Map.of("anyOtherField", "anyOtherValue"))
                                        .build())
                                .build())
                        .build(),
                Payment.builder(payment)
                        .transactions(new ArrayList<>())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource
    public void klarnaExceptionIfNoKlarnaOrderId(Order order) {
        final Payment payment = requireNonNull(getPayment());
        final String sessionId = UUID.randomUUID().toString();
        payment.getCustom().getFields().values().put(SESSION_ID, sessionId);

        when(orderService.findOrderById(order.getId())).thenReturn(Uni.createFrom().item(order));

        final var ex = assertThrows(KlarnaCtException.class,
                () -> updatePaymentService.updatePayment(new PaymentRequest(
                        "Update",
                        PaymentReference.builder()
                                .id(payment.getId())
                                .obj(payment)
                                .build()
                )).await().indefinitely());

        assertEquals(format("Klarna Order Id required for Order %s", order.getId()), ex.getMessage());
    }

    private static Stream<Order> klarnaExceptionIfNoKlarnaOrderId() {
        final Order order = requireNonNull(getOrder());
        return Stream.of(
                Order.builder(order)
                        .custom(null)
                        .build(),
                Order.builder(order)
                        .custom(CustomFields.builder()
                                .build())
                        .build(),
                Order.builder(order)
                        .custom(CustomFields.builder()
                                .fields(FieldContainer.builder()
                                        .build())
                                .build())
                        .build(),
                Order.builder(order)
                        .custom(CustomFields.builder()
                                .fields(FieldContainer.builder()
                                        .values(Map.of("anyOtherKey", "anyOtherValue"))
                                        .build())
                                .build())
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource
    public void successfullyCancelAuth(Payment payment) {
        final var paymentReference = PaymentReference.builder()
                .id(payment.getId())
                .obj(payment)
                .build();
        final String transactionId = payment.getTransactions().get(0).getId();

        if (getOrderId(payment) == null) {
            when(klarnaPaymentService.cancelAuthorisation(getAuthToken(payment)))
                    .thenReturn(Uni.createFrom().item(() -> Response.noContent().build()));
        } else {
            final Order order = getOrder();
            when(orderService.findOrderById(getOrderId(payment)))
                    .thenReturn(Uni.createFrom().item(() -> order));
            when(klarnaOrderService.releaseRemainingAuthorisation(any(String.class)))
                    .thenReturn(Uni.createFrom().item(() -> Response.noContent().build()));
        }

        final var res = updatePaymentService.updatePayment(new PaymentRequest(
                "Update",
                paymentReference
        )).await().indefinitely();

        assertFalse(res.getActions().isEmpty());
        assertTrue(res.getActions().stream().anyMatch(a -> {
            if (!(a instanceof PaymentSetCustomFieldAction)) return false;
            final var scf = (PaymentSetCustomFieldAction) a;
            return SET_CUSTOM_FIELD.equals(scf.getAction())
                    && KLARNA_AUTH_TOKEN.equals(scf.getName())
                    && scf.getValue() == null;
        }));
        assertTrue(res.getActions().stream().anyMatch(a -> {
            if (!(a instanceof PaymentChangeTransactionStateAction)) return false;
            final var scf = (PaymentChangeTransactionStateAction) a;
            return CHANGE_TRANSACTION_STATE.equals(scf.getAction())
                    && TransactionState.SUCCESS == scf.getState()
                    && transactionId.equals(scf.getTransactionId());
        }));
    }

    private static Stream<Payment> successfullyCancelAuth() {
        final String transactionId = UUID.randomUUID().toString();
        final Transaction cancelAuth = Transaction.builder()
                .id(transactionId)
                .state(TransactionState.INITIAL)
                .type(TransactionType.CANCEL_AUTHORIZATION)
                .build();

        final Payment payment = requireNonNull(getPayment());
        final String sessionId = UUID.randomUUID().toString();
        payment.getCustom().getFields().values().put(SESSION_ID, sessionId);

        return Stream.of(
                Payment.builder(payment)
                        .transactions(List.of(
                                cancelAuth,
                                Transaction.builder()
                                        .id(UUID.randomUUID().toString())
                                        .state(TransactionState.SUCCESS)
                                        .type(TransactionType.CHARGE)
                                        .build()
                        )).build()
        );
    }

    @Test
    public void noCaptureOrderToBeCancelled() {
        final Payment payment = requireNonNull(getPayment());
        final String sessionId = UUID.randomUUID().toString();
        payment.getCustom().getFields().values().put(SESSION_ID, sessionId);
        final String transactionId = UUID.randomUUID().toString();
        final Transaction cancelAuth = Transaction.builder()
                .id(transactionId)
                .state(TransactionState.INITIAL)
                .type(TransactionType.CANCEL_AUTHORIZATION)
                .build();
        payment.getTransactions().add(cancelAuth);

        final var paymentReference = PaymentReference.builder()
                .id(payment.getId())
                .obj(payment)
                .build();

        final var ex = assertThrows(KlarnaCtException.class,
                () -> updatePaymentService.updatePayment(new PaymentRequest(
                        "Update",
                        paymentReference
                )).await().indefinitely());

        assertEquals(
                "No capture for this order. Order should be cancelled.",
                ex.getMessage()
        );
    }

    @Test
    public void releaseWithoutKlarnaOrderId() {
        final Payment payment = requireNonNull(getPayment());
        final String sessionId = UUID.randomUUID().toString();
        payment.getCustom().getFields().values().put(SESSION_ID, sessionId);
        payment.getTransactions().addAll(List.of(
                Transaction.builder()
                        .id(UUID.randomUUID().toString())
                        .type(TransactionType.CHARGE)
                        .state(TransactionState.SUCCESS)
                        .build(),
                Transaction.builder()
                        .id(UUID.randomUUID().toString())
                        .type(TransactionType.CANCEL_AUTHORIZATION)
                        .state(TransactionState.INITIAL)
                        .build()
        ));
        final var paymentReference = PaymentReference.builder()
                .id(payment.getId())
                .obj(payment)
                .build();

        final Order order = requireNonNull(getOrder());
        order.setCustom(null);
        when(orderService.findOrderById(getOrderId(payment)))
                .thenReturn(Uni.createFrom().item(() -> order));

        final var ex = assertThrows(KlarnaCtException.class,
                () -> updatePaymentService.updatePayment(new PaymentRequest(
                        "Update",
                        paymentReference
                )).await().indefinitely());

        assertEquals(format(
                        "Klarna Order Id required to release authorisation for %s:%s",
                        order.getId(),
                        payment.getId()),
                ex.getMessage()
        );
    }

    @Test
    public void failureToCancelAuthRecorded() {
        final Payment payment = requireNonNull(getPayment());
        final String transactionId = UUID.randomUUID().toString();
        final String sessionId = UUID.randomUUID().toString();
        final Transaction cancelAuth = Transaction.builder()
                .id(transactionId)
                .state(TransactionState.INITIAL)
                .type(TransactionType.CANCEL_AUTHORIZATION)
                .build();

        final PaymentReference paymentReference = PaymentReference.builder()
                .obj(Payment.builder(payment)
                        .transactions(List.of(
                                Transaction.builder(cancelAuth)
                                        .state(TransactionState.FAILURE)
                                        .build(),
                                Transaction.builder(cancelAuth)
                                        .type(TransactionType.AUTHORIZATION)
                                        .state(TransactionState.INITIAL)
                                        .build(),
                                cancelAuth
                        )).custom(CustomFields.builder()
                                .fields(FieldContainer.builder()
                                        .values(Map.of(SESSION_ID, sessionId))
                                        .build())
                                .build())
                        .build())
                .build();

        final Cart cart = getCart();
        when(cartService.findCartForPayment(any(Payment.class))).thenReturn(Uni.createFrom().item(() -> cart));
        when(klarnaPaymentService.updateSession(any(String.class), any(CreditSession.class)))
                .thenReturn(Uni.createFrom().item(() -> Response.ok().build()));

        when(klarnaPaymentService.cancelAuthorisation(getAuthToken(payment)))
                .thenReturn(Uni.createFrom().item(() -> Response.status(Response.Status.NOT_FOUND).build()));

        when(klarnaPaymentServiceProvider.get(any())).thenReturn(klarnaPaymentService);

        final var res = updatePaymentService.updatePayment(new PaymentRequest(
                "Update",
                paymentReference
        )).await().indefinitely();

        assertFalse(res.getActions().isEmpty());
        assertTrue(res.getActions().stream().anyMatch(a -> {
            if (!(a instanceof PaymentChangeTransactionStateAction)) return false;
            final var scf = (PaymentChangeTransactionStateAction) a;
            return CHANGE_TRANSACTION_STATE.equals(scf.getAction())
                    && TransactionState.FAILURE == scf.getState()
                    && transactionId.equals(scf.getTransactionId());
        }));
    }
}
