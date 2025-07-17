package com.e2x.klarnact.commercetools.order;

import com.commercetools.api.models.order.Order;
import com.e2x.klarnact.commercetools.client.CtClient;
import com.e2x.klarnact.exception.NotFoundException;
import io.smallrye.mutiny.Uni;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;

import static java.lang.String.format;

@RequiredArgsConstructor
@ApplicationScoped
public class OrderServiceImpl implements OrderService {
    private final CtClient ctClient;

    @Override
    public Uni<Order> findOrderById(String id) {
        return Uni.createFrom().completionStage(() -> ctClient.request(requestBuilder ->
                requestBuilder.orders().withId(id)
                        .get()
                        .addExpand("paymentInfo.payments[*].obj")
                        .execute()
                        .handleAsync((res, ex) -> {
                            if (res == null || res.getBody() == null) {
                                throw new NotFoundException(format("Order Not Found with id of %s", id));
                            } else return res.getBody();
                        })));
    }
}
