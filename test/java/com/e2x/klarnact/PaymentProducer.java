package com.e2x.klarnact;

import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.PaymentBuilder;
import com.commercetools.api.models.payment.PaymentMethodInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

import static com.e2x.klarnact.PaymentInterfaceName.KLARNA;
import static com.e2x.klarnact.config.ObjectMapperConfig.getObjectMapper;

public class PaymentProducer {
    private static final Logger LOG = LoggerFactory.getLogger(PaymentProducer.class);

    private PaymentProducer() {
    }

    public static PaymentBuilder klarnaPaymentBuilder() {
        return Payment.builder()
                .anonymousId(UUID.randomUUID().toString())
                .paymentMethodInfo(PaymentMethodInfo.builder()
                        .paymentInterface(KLARNA.getCode())
                        .build()
                );
    }

    public static Payment getPayment() {
        try {
            return getObjectMapper().readValue(Paths.get("src/test/resources/Payment.json").toFile(), Payment.class);
        } catch (IOException e) {
            LOG.error("Unable to read Payment.json", e);
            return null;
        }
    }
}
