package com.e2x.klarnact.klarna.client;

import com.e2x.klarnact.exception.KlarnaCtException;
import com.e2x.klarnact.klarna.model.order.Order;
import com.e2x.klarnact.klarna.model.order.OrderCreated;
import com.e2x.klarnact.klarna.model.payment.CreditSession;
import com.e2x.klarnact.klarna.model.payment.SessionCreated;
import io.quarkus.arc.AlternativePriority;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static com.e2x.klarnact.payment.PaymentResourceTestInt.CLIENT_TOKEN;

@AlternativePriority(1)
@ApplicationScoped
public class KlarnaPaymentServiceMock implements KlarnaPaymentService {

    @Override
    public Uni<SessionCreated> createSession(CreditSession creditSession) {
        final var exception = nonNullFields().stream()
                .filter(it -> it.apply(creditSession) == null)
                .findFirst()
                .map(it -> new KlarnaCtException("Invalid Input in Credit Session"));

        if (exception.isPresent()) {
            return Uni.createFrom().failure(exception::get);
        } else if (creditSession.getOrderLines().isEmpty()) {
            return Uni.createFrom().failure(() -> new KlarnaCtException("No Order Lines Provided"));
        } else {
            return Uni.createFrom().item(() ->
                    SessionCreated.builder()
                            .sessionId(UUID.randomUUID().toString())
                            .clientToken(CLIENT_TOKEN)
                            .build()
            );
        }
    }

    @Override
    public Uni<Response> updateSession(String sessionId, CreditSession creditSession) {
        return Uni.createFrom().item(Response.ok().build());
    }

    @Override
    public Uni<OrderCreated> createOrder(String authToken, Order order) {
        return Uni.createFrom().item(() -> OrderCreated.builder().orderId(UUID.randomUUID().toString()).build());
    }

    @Override
    public Uni<Response> cancelAuthorisation(String authToken) {
        return Uni.createFrom().item(() -> Response.noContent().build());
    }

    private Collection<Function<CreditSession, Object>> nonNullFields() {
        return List.of(
                CreditSession::getLocale,
                CreditSession::getPurchaseCountry,
                CreditSession::getPurchaseCurrency,
                CreditSession::getOrderAmount,
                CreditSession::getOrderLines
        );
    }
}
