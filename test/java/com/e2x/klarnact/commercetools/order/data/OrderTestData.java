package com.e2x.klarnact.commercetools.order.data;

import com.commercetools.api.models.order.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

import static com.e2x.klarnact.config.ObjectMapperConfig.getObjectMapper;

public class OrderTestData {
    private static final Logger LOG = LoggerFactory.getLogger(OrderTestData.class);

    private OrderTestData() {
    }

    public static Order getOrder() {
        try {
            return getObjectMapper().readValue(Paths.get("src/test/resources/Order.json").toFile(), Order.class);
        } catch (IOException e) {
            LOG.error("Unable to read Order.json", e);
            return null;
        }
    }
}
