package com.e2x.klarnact.payment;

import com.commercetools.api.models.cart.Cart;
import com.e2x.klarnact.CustomFieldHelper;

import static com.e2x.klarnact.CustomFields.Order.EXTRA_MERCHANT_DATA;

public class CartHelper {
    private CartHelper() {
    }

    public static <T> T getCustomField(Cart cart, String fieldName, Class<T> clazz) {
        if (cart == null) return null;
        return CustomFieldHelper.getCustomField(cart.getCustom(), fieldName, clazz);
    }

    public static String getExtraMerchantData(Cart cart) {
        return getCustomField(cart, EXTRA_MERCHANT_DATA, String.class);
    }
}
