package com.e2x.klarnact.payment.create;

import com.commercetools.api.models.payment.PaymentSetCustomTypeAction;
import com.commercetools.api.models.payment.PaymentUpdate;
import com.commercetools.api.models.type.FieldContainer;
import com.commercetools.api.models.type.TypeResourceIdentifier;
import com.e2x.klarnact.commercetools.cart.CartService;
import com.e2x.klarnact.exception.KlarnaCtException;
import com.e2x.klarnact.klarna.client.provider.KlarnaPaymentServiceProvider;
import com.e2x.klarnact.payment.PaymentRequest;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.List;

import static com.e2x.klarnact.CustomFields.Payment.*;
import static com.e2x.klarnact.CustomTypes.PAYMENT_KLARNA_TYPE;
import static com.e2x.klarnact.PaymentInterfaceName.KLARNA;
import static com.e2x.klarnact.payment.PaymentHelper.extractPayment;
import static java.lang.String.format;

@Slf4j
@ApplicationScoped
@AllArgsConstructor
public class CreatePaymentService {
    private final CartService cartService;
    private final KlarnaPaymentServiceProvider paymentServiceProvider;
    private final CartToSessionMapper cartToSessionMapper;

    public Uni<PaymentUpdate> createPayment(PaymentRequest createPaymentRequest) {

        if (!"create".equalsIgnoreCase(createPaymentRequest.getAction())) {
            throw new KlarnaCtException("Incorrect request type for endpoint");
        }

        val payment = extractPayment(createPaymentRequest);

        if (KLARNA.notOfInterest(payment)) {
            log.debug("Create of non Klarna payment received");
            return Uni.createFrom().item(PaymentUpdate.builder()
                    .version(payment.getVersion())
                    .actions(List.of())
                    .build()
            );
        }

        return cartService.findCartForPayment(payment)
                .flatMap(cart -> {
                    if (cart.getLocale() == null) {
                        throw new KlarnaCtException("Locale Required on Cart");
                    }
                    return cartToSessionMapper.mapToSession(cart, payment);
                })
                .flatMap(creditSession -> paymentServiceProvider.get(creditSession.getPurchaseCountry()).createSession(creditSession))
                .map(session -> {
                            if (session == null) {
                                throw new KlarnaCtException(format("Unable to create Klarna Session for Payment %s", payment.getId()));
                            }

                            final var customFields = new HashMap<String, Object>();
                            customFields.put(SESSION_ID, session.getSessionId());
                            customFields.put(KLARNA_CLIENT_TOKEN, session.getClientToken());

                            return PaymentUpdate.builder()
                                    .actions(List.of(
                                            PaymentSetCustomTypeAction.builder()
                                                    .type(TypeResourceIdentifier.builder()
                                                            .key(PAYMENT_KLARNA_TYPE)
                                                            .build())
                                                    .fields(FieldContainer.builder()
                                                            .values(customFields)
                                                            .build())
                                                    .build()
                                    )).build();
                        }
                );
    }
}
