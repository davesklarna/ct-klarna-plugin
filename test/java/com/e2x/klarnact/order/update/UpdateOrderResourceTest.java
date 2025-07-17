package com.e2x.klarnact.order.update;

import com.commercetools.api.models.order.OrderReference;
import com.commercetools.api.models.order.OrderUpdate;
import com.e2x.klarnact.order.create.OrderRequest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UpdateOrderResourceTest {

    @Mock
    UpdateOrderService updateOrderService;

    UpdateOrderResource updateOrderResource;

    @BeforeEach
    void setup() {
        updateOrderResource = new UpdateOrderResource(updateOrderService);
    }

    @Test
    void passThroughToService() {

        when(updateOrderService.updateOrder(any(OrderRequest.class)))
                .thenReturn(Uni.createFrom().item(() -> OrderUpdate.builder()
                        .actions(new ArrayList<>())
                        .build()));

        final var res = updateOrderResource.updateOrder(
                new OrderRequest("update", OrderReference.of())
        ).await().atMost(Duration.of(1000, ChronoUnit.MILLIS));

        assertNotNull(res);
        assertNotNull(res.getActions());
        assertTrue(res.getActions().isEmpty());
    }
}
