package com.e2x.klarnact.commercetools.cart;

import com.commercetools.api.models.payment.Payment;
import com.e2x.klarnact.commercetools.client.CtClient;
import com.e2x.klarnact.config.CommerceToolsConfigTest;
import com.e2x.klarnact.exception.KlarnaCtException;
import com.e2x.klarnact.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.lang.String.format;
import static java.time.Duration.of;
import static java.time.temporal.ChronoUnit.MILLIS;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    CtClient ctClient = new CtClient(new CommerceToolsConfigTest());

    CartServiceImpl cartService;

    @BeforeEach
    public void setup() {
        cartService = new CartServiceImpl(ctClient);
    }

    @Test
    public void throwsKlarnaExceptionWhenNoParametersToMatchCart() {
        final Payment payment = Payment.builder().build();

        final var res = assertThrows(
                KlarnaCtException.class, () -> cartService.findCartForPayment(payment)
                        .await()
                        .atMost(of(1000, MILLIS))
        );

        assertEquals(BAD_REQUEST, res.getStatus());
        assertEquals("No parameters available to match payment to cart", res.getMessage());
    }

    @Test
    public void throwsKlarnaExceptionWhenNoCartRetrieved() {
        final String anonymousId = UUID.randomUUID().toString();
        final Payment payment = Payment.builder()
                .anonymousId(anonymousId)
                .build();

        final var res = assertThrows(
                NotFoundException.class, () -> cartService.findCartForPayment(payment)
                        .await()
                        .atMost(of(1000, MILLIS))
        );

        assertEquals(NOT_FOUND, res.getStatus());
        assertEquals(format("Cart Not Found with anonymousId of %s", anonymousId), res.getMessage());
    }

    @Test
    public void throwsKlarnaCtExceptionWhenNoCartWithId() {
        final String cartId = UUID.randomUUID().toString();
        final var res = assertThrows(
                NotFoundException.class, () -> cartService.findCartById(cartId)
                        .await()
                        .atMost(of(1000, MILLIS))
        );

        assertEquals(NOT_FOUND, res.getStatus());
        assertEquals(format("Cart Not Found with id of %s", cartId), res.getMessage());
    }
}