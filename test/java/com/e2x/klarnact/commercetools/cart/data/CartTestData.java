package com.e2x.klarnact.commercetools.cart.data;

import com.commercetools.api.models.cart.Cart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

import static com.e2x.klarnact.config.ObjectMapperConfig.getObjectMapper;

public class CartTestData {
    private static final Logger LOG = LoggerFactory.getLogger(CartTestData.class);

    private CartTestData() {
    }

    public static Cart getCart() {
        try {
            return getObjectMapper().readValue(Paths.get("src/test/resources/Cart.json").toFile(), Cart.class);
        } catch (IOException e) {
            LOG.error("Unable to read Cart.json", e);
            return null;
        }
    }
}
