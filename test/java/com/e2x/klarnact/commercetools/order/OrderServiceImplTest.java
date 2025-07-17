package com.e2x.klarnact.commercetools.order;

import com.e2x.klarnact.commercetools.client.CtClient;
import com.e2x.klarnact.config.CommerceToolsConfigTest;
import com.e2x.klarnact.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.lang.String.format;
import static java.time.Duration.of;
import static java.time.temporal.ChronoUnit.MILLIS;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    CtClient ctClient = new CtClient(new CommerceToolsConfigTest());

    OrderServiceImpl orderService;

    @BeforeEach
    public void setup() {
        orderService = new OrderServiceImpl(ctClient);
    }

    @Test
    public void throwsNotFoundWhenNoOrderWithId() {
        final String orderId = UUID.randomUUID().toString();
        final var res = assertThrows(
                NotFoundException.class, () -> orderService.findOrderById(orderId)
                        .await()
                        .atMost(of(1000, MILLIS))
        );

        assertEquals(NOT_FOUND, res.getStatus());
        assertEquals(format("Order Not Found with id of %s", orderId), res.getMessage());
    }
}