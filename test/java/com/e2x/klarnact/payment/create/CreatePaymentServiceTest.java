package com.e2x.klarnact.payment.create;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.common.CentPrecisionMoney;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.PaymentMethodInfo;
import com.commercetools.api.models.payment.PaymentReference;
import com.commercetools.api.models.payment.PaymentSetCustomTypeAction;
import com.e2x.klarnact.commercetools.cart.CartService;
import com.e2x.klarnact.commercetools.config.CommerceToolsMapperConfig;
import com.e2x.klarnact.config.CommerceToolsMapperConfigTest;
import com.e2x.klarnact.exception.KlarnaCtException;
import com.e2x.klarnact.exception.NotFoundException;
import com.e2x.klarnact.klarna.client.KlarnaPaymentService;
import com.e2x.klarnact.klarna.client.provider.KlarnaPaymentServiceProvider;
import com.e2x.klarnact.klarna.model.payment.CreditSession;
import com.e2x.klarnact.klarna.model.payment.SessionCreated;
import com.e2x.klarnact.mapper.OrderLineMapper;
import com.e2x.klarnact.payment.PaymentRequest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.Response;
import java.util.UUID;

import static com.e2x.klarnact.CustomFields.Payment.KLARNA_CLIENT_TOKEN;
import static com.e2x.klarnact.PaymentProducer.klarnaPaymentBuilder;
import static com.e2x.klarnact.commercetools.cart.data.CartTestData.getCart;
import static com.e2x.klarnact.commercetools.payment.PaymentUtils.ANONYMOUS_ID;
import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePaymentServiceTest {

    @Mock
    CartService cartService;

    @Mock
    KlarnaPaymentService klarnaPaymentService;

    @Mock
    KlarnaPaymentServiceProvider klarnaPaymentServiceProvider;

    CreatePaymentService createPaymentService;

    @BeforeEach
    public void setup() {
        lenient().when(klarnaPaymentServiceProvider.get(any())).thenReturn(klarnaPaymentService);

        final CommerceToolsMapperConfig commerceToolsMapperConfig = new CommerceToolsMapperConfigTest();
        final OrderLineMapper orderLineMapper = new OrderLineMapper(commerceToolsMapperConfig);
        final CartToSessionMapper cartToSessionMapper = new CartToSessionMapper(orderLineMapper);

        createPaymentService = new CreatePaymentService(
                cartService,
                klarnaPaymentServiceProvider,
                cartToSessionMapper
        );
    }

    @Test
    public void exceptionThrownWhenNoPaymentReference() {

        final KlarnaCtException ex1 = assertThrows(KlarnaCtException.class,
                () -> createPaymentService.createPayment(new PaymentRequest(
                        "Create",
                        null
                )).await().indefinitely());

        assertEquals(Response.Status.BAD_REQUEST, ex1.getStatus());
        assertEquals("Payment Reference must not be null", ex1.getMessage());

        final PaymentReference paymentReference = PaymentReference.builder()
                .build();

        final KlarnaCtException ex2 = assertThrows(KlarnaCtException.class,
                () -> createPaymentService.createPayment(new PaymentRequest(
                        "Create",
                        paymentReference
                )).await().indefinitely());

        assertEquals(Response.Status.BAD_REQUEST, ex2.getStatus());
        assertEquals("Payment Reference must not be null", ex2.getMessage());
    }

    @Test
    public void exceptionThrownWhenNoLocaleOnCart() {
        final Payment payment = klarnaPaymentBuilder().build();
        final PaymentReference paymentReference = PaymentReference.builder().obj(payment).build();
        final Cart cart = Cart.builder().build();

        when(cartService.findCartForPayment(any(Payment.class))).thenReturn(
                Uni.createFrom().item(() -> cart)
        );

        final KlarnaCtException exception = assertThrows(KlarnaCtException.class,
                () -> createPaymentService.createPayment(new PaymentRequest(
                        "Create",
                        paymentReference
                )).await().indefinitely());

        assertEquals(Response.Status.BAD_REQUEST, exception.getStatus());
        assertEquals("Locale Required on Cart", exception.getMessage());
    }

    @Test
    public void handlesNoCartFoundExceptionFromCartService() {
        final Payment payment = klarnaPaymentBuilder().build();
        final PaymentReference paymentReference = PaymentReference.builder().obj(payment).build();
        final String errorMessage = format(
                "Cart Not Found with %s of %s",
                ANONYMOUS_ID,
                payment.getAnonymousId()
        );

        when(cartService.findCartForPayment(any(Payment.class))).thenReturn(
                Uni.createFrom().failure(() -> new NotFoundException(errorMessage))
        );

        final KlarnaCtException exception = assertThrows(KlarnaCtException.class,
                () -> createPaymentService.createPayment(new PaymentRequest(
                        "Create",
                        paymentReference
                )).await().indefinitely());

        assertEquals(Response.Status.NOT_FOUND, exception.getStatus());
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    public void successfullyReturnsCustomTypeActionWithClientToken() {

        final Payment payment = klarnaPaymentBuilder()
                .amountPlanned(CentPrecisionMoney.builder()
                        .centAmount(149250L)
                        .currencyCode("EUR")
                        .build())
                .build();
        final PaymentReference paymentReference = PaymentReference.builder()
                .obj(payment)
                .build();
        final Cart cart = getCart();

        final String clientToken = UUID.randomUUID().toString();
        final String actionAction = "setCustomType";
        final String actionName = KLARNA_CLIENT_TOKEN;

        when(cartService.findCartForPayment(any(Payment.class))).thenReturn(
                Uni.createFrom().item(() -> cart)
        );

        when(klarnaPaymentService.createSession(any(CreditSession.class)))
                .thenReturn(Uni.createFrom().item(() -> SessionCreated.builder()
                        .sessionId(UUID.randomUUID().toString())
                        .clientToken(clientToken)
                        .build()
                ));

        final var res = createPaymentService.createPayment(new PaymentRequest(
                "Create",
                paymentReference
        )).await().indefinitely();

        assertFalse(res.getActions().isEmpty());
        assertTrue(res.getActions().stream().anyMatch(a -> {
            if (!(a instanceof PaymentSetCustomTypeAction)) return false;
            final var cta = (PaymentSetCustomTypeAction) a;
            return actionAction.equals(a.getAction())
                    && cta.getFields().values().containsKey(actionName)
                    && clientToken.equals(cta.getFields().values().get(actionName));
        }));
    }

    @Test
    public void successfullyIgnoresNonKlarnaPayments() {

        final Payment p1 = Payment.builder()
                .anonymousId(UUID.randomUUID().toString())
                .amountPlanned(CentPrecisionMoney.builder()
                        .centAmount(149250L)
                        .currencyCode("EUR")
                        .build())
                .build();
        final PaymentReference pr1 = PaymentReference.builder()
                .obj(p1)
                .build();

        final var res1 = createPaymentService.createPayment(new PaymentRequest(
                "Create",
                pr1
        )).await().indefinitely();

        assertTrue(res1.getActions().isEmpty());

        final Payment p2 = Payment.builder()
                .anonymousId(UUID.randomUUID().toString())
                .amountPlanned(CentPrecisionMoney.builder()
                        .centAmount(149250L)
                        .currencyCode("EUR")
                        .build())
                .paymentMethodInfo(PaymentMethodInfo.builder().build())
                .build();
        final PaymentReference pr2 = PaymentReference.builder()
                .obj(p2)
                .build();

        final var res2 = createPaymentService.createPayment(new PaymentRequest(
                "Create",
                pr2
        )).await().indefinitely();

        assertTrue(res2.getActions().isEmpty());
    }

    @Test
    public void klarnaExceptionIfNotAPaymentCreate() {

        final var ex = assertThrows(KlarnaCtException.class,
                () -> createPaymentService.createPayment(new PaymentRequest(
                        "Update",
                        PaymentReference.of()
                )).await().indefinitely());

        assertEquals("Incorrect request type for endpoint", ex.getMessage());
    }

    @Test
    public void klarnaExceptionIfNoSessionCreated() {

        final Payment payment = klarnaPaymentBuilder()
                .amountPlanned(CentPrecisionMoney.builder()
                        .centAmount(149250L)
                        .currencyCode("EUR")
                        .build())
                .build();
        final PaymentReference paymentReference = PaymentReference.builder()
                .obj(payment)
                .build();
        final Cart cart = getCart();

        when(cartService.findCartForPayment(any(Payment.class))).thenReturn(
                Uni.createFrom().item(() -> cart)
        );

        when(klarnaPaymentService.createSession(any(CreditSession.class)))
                .thenReturn(Uni.createFrom().item(() -> null));

        final var res = assertThrows(KlarnaCtException.class, () -> createPaymentService.createPayment(new PaymentRequest(
                "Create",
                paymentReference
        )).await().indefinitely());

        assertEquals(format("Unable to create Klarna Session for Payment %s", payment.getId()), res.getMessage());
    }
}
