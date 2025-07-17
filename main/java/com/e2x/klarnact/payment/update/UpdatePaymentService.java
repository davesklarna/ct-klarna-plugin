package com.e2x.klarnact.payment.update;

import com.commercetools.api.models.payment.*;
import com.e2x.klarnact.commercetools.cart.CartService;
import com.e2x.klarnact.commercetools.order.OrderService;
import com.e2x.klarnact.exception.KlarnaCtException;
import com.e2x.klarnact.klarna.client.provider.KlarnaCaptureServiceProvider;
import com.e2x.klarnact.klarna.client.provider.KlarnaOrderServiceProvider;
import com.e2x.klarnact.klarna.client.provider.KlarnaPaymentServiceProvider;
import com.e2x.klarnact.klarna.client.provider.KlarnaRefundServiceProvider;
import com.e2x.klarnact.klarna.model.order.Capture;
import com.e2x.klarnact.klarna.model.order.Refund;
import com.e2x.klarnact.payment.PaymentRequest;
import com.e2x.klarnact.payment.create.CartToSessionMapper;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Stream;

import static com.e2x.klarnact.CustomFields.Payment.KLARNA_AUTH_TOKEN;
import static com.e2x.klarnact.PaymentInterfaceName.KLARNA;
import static com.e2x.klarnact.klarna.HeaderId.CAPTURE_ID;
import static com.e2x.klarnact.klarna.HeaderId.REFUND_ID;
import static com.e2x.klarnact.order.OrderHelper.getKlarnaOrderId;
import static com.e2x.klarnact.payment.PaymentHelper.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.Response.Status.Family;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
@ApplicationScoped
@AllArgsConstructor
public class UpdatePaymentService {
    private final CartService cartService;
    private final OrderService orderService;
    private final KlarnaCaptureServiceProvider klarnaCaptureServiceProvider;
    private final KlarnaOrderServiceProvider klarnaOrderServiceProvider;
    private final KlarnaPaymentServiceProvider paymentServiceProvider;
    private final KlarnaRefundServiceProvider klarnaRefundServiceProvider;
    private final CartToSessionMapper cartToSessionMapper;
    private final CaptureMapper captureMapper;
    private final RefundMapper refundMapper;

    public Uni<PaymentUpdate> updatePayment(PaymentRequest updatePaymentRequest) {

        if (!"update".equalsIgnoreCase(updatePaymentRequest.getAction())) {
            throw new KlarnaCtException("Incorrect request type for endpoint");
        }

        val payment = extractPayment(updatePaymentRequest);

        if (KLARNA.notOfInterest(payment)) {
            log.debug("Update of non Klarna payment received");
            return emptyUpdate(payment);
        }

        return sessionUpdateResponse(payment)
                .flatMap(r -> {
                    if (Family.SUCCESSFUL == r.getStatusInfo().getFamily()) {
                        final var cancelAuth = getCancelAuthTransaction(payment);
                        final String orderId = getOrderId(payment);

                        if (cancelAuth.isPresent()) {
                            log.debug("Cancel Authorisation request received");
                            if (isBlank(orderId)) {
                                return cancelAuth(payment, cancelAuth.get());
                            } else if (payment.getTransactions().stream().anyMatch(CAPTURED_TRANSACTION)) {
                                return releaseRemainingAuthorisation(payment, cancelAuth.get());
                            } else {
                                throw new KlarnaCtException("No capture for this order. Order should be cancelled.");
                            }
                        } else if (isBlank(orderId)) {
                            log.debug("Update payment received without Order Id");
                            // For example when FE updates payment with authorisation token
                            return emptyUpdate(payment);
                        }

                        if (capturesOrRefunds(payment).findAny().isEmpty()) {
                            log.debug("Update payment received without matching transactions");
                            return emptyUpdate(payment);
                        }

                        return createTransactions(payment);
                    } else {
                        throw new KlarnaCtException("Unable to update Klarna Session");
                    }
                });
    }

    private Uni<Response> sessionUpdateResponse(Payment payment) {
        final Uni<Response> updateSessionDecider;
        if (isBlank(getOrderId(payment))) {
            //no order yet so can update session
            updateSessionDecider = cartService.findCartForPayment(payment)
                    .flatMap(cart -> {
                        if (cart.getLocale() == null) {
                            throw new KlarnaCtException("Locale Required on Cart");
                        }
                        return cartToSessionMapper.mapToSession(cart, payment);
                    })
                    .flatMap(session -> paymentServiceProvider.get(session.getPurchaseCountry()).updateSession(getSessionId(payment), session));
        } else {
            // order already created so cannot update session
            updateSessionDecider = Uni.createFrom().item(Response.noContent().build());
        }
        return updateSessionDecider;
    }

    private Uni<PaymentUpdate> emptyUpdate(Payment payment) {
        return Uni.createFrom().item(PaymentUpdate.builder()
                .version(payment.getVersion())
                .actions(List.of())
                .build());
    }

    private Uni<PaymentUpdate> cancelAuth(Payment payment, Transaction cancelAuth) {
        final String authToken = getAuthToken(payment);
        return cartService.findCartForPayment(payment).flatMap(cart ->
            paymentServiceProvider.get(cart.getCountry()).cancelAuthorisation(authToken)
                .map(r -> getCancelAuthActions(r, cancelAuth))
                .map(actions -> PaymentUpdate.builder()
                        .actions(actions)
                        .version(payment.getVersion())
                        .build())
        );
    }

    private List<PaymentUpdateAction> getCancelAuthActions(Response r, Transaction cancelAuth) {
        if (Family.SUCCESSFUL == r.getStatusInfo().getFamily()) {
            return List.of(
                    PaymentSetCustomFieldAction.builder()
                            .name(KLARNA_AUTH_TOKEN)
                            .build(),
                    PaymentChangeTransactionStateAction.builder()
                            .transactionId(cancelAuth.getId())
                            .state(TransactionState.SUCCESS)
                            .build());
        } else {
            return List.of(PaymentChangeTransactionStateAction.builder()
                    .transactionId(cancelAuth.getId())
                    .state(TransactionState.FAILURE)
                    .build());
        }
    }

    private Uni<PaymentUpdate> releaseRemainingAuthorisation(Payment payment, Transaction cancelAuth) {
        return orderService.findOrderById(getOrderId(payment)).flatMap(order -> {
                    final String klarnaOrderId = getKlarnaOrderId(order);
                    if (klarnaOrderId == null) {
                        throw new KlarnaCtException(format(
                                "Klarna Order Id required to release authorisation for %s:%s", order.getId(), payment.getId()
                        ));
                    } else {
                        return klarnaOrderServiceProvider.get(order.getCountry()).releaseRemainingAuthorisation(klarnaOrderId)
                                .map(r -> getCancelAuthActions(r, cancelAuth))
                                .map(actions -> PaymentUpdate.builder()
                                        .actions(actions)
                                        .version(payment.getVersion())
                                        .build());
                    }
                }
        );
    }

    private Uni<PaymentUpdate> createTransactions(Payment payment) {
        return orderService.findOrderById(getOrderId(payment))
                .flatMap(order -> {
                    final String klarnaOrderId = getKlarnaOrderId(order);
                    if (klarnaOrderId == null) {
                        throw new KlarnaCtException(format("Klarna Order Id required for Order %s", order.getId()));
                    }

                    val transactions = capturesOrRefunds(payment)
                            .map(t -> {
                                switch (t.getType().getJsonName()) {
                                    case "Charge":
                                        return captureMapper.mapToCapture(order, t);
                                    case "Refund":
                                        return refundMapper.mapToRefund(order, t);
                                    default:
                                        throw new KlarnaCtException(format("Unsupported type of transaction %s", t.getType()));
                                }
                            })
                            .collect(toList());

                    return Multi.createFrom().iterable((transactions))
                            .onItem()
                            .transformToUniAndConcatenate(t -> {
                                if (t instanceof Capture) {
                                    val c = (Capture) t;
                                    return klarnaCaptureServiceProvider.get(order.getCountry()).createCapture(
                                            klarnaOrderId,
                                            c
                                    ).map(res -> c.toBuilder().id(res.getHeaderString(CAPTURE_ID)).build()
                                    ).onFailure().recoverWithItem(c);
                                } else if (t instanceof Refund) {
                                    val r = (Refund) t;
                                    return klarnaRefundServiceProvider.get(order.getCountry()).createRefund(
                                            klarnaOrderId,
                                            r
                                    ).map(res -> r.toBuilder().id(res.getHeaderString(REFUND_ID)).build()
                                    ).onFailure().recoverWithItem(r);
                                } else return null;
                            })
                            .collect().asList()
                            .map(res -> {
                                val actions = res.stream()
                                        .flatMap(capture -> capture.getId() == null ?
                                                Stream.of(PaymentChangeTransactionStateAction.builder()
                                                        .transactionId(capture.getReference())
                                                        .state(TransactionState.FAILURE)
                                                        .build()) :
                                                Stream.of(PaymentChangeTransactionStateAction.builder()
                                                                .transactionId(capture.getReference())
                                                                .state(TransactionState.SUCCESS)
                                                                .build(),
                                                        PaymentChangeTransactionInteractionIdAction.builder()
                                                                .transactionId(capture.getReference())
                                                                .interactionId(capture.getId())
                                                                .build()
                                                )
                                        ).collect(toList());
                                return PaymentUpdate.builder()
                                        .version(payment.getVersion())
                                        .actions(actions).build();
                            });
                });
    }
}
