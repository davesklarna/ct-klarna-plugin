package com.e2x.klarnact.mapper;

import com.e2x.klarnact.config.LocaleConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class ShippingChargeMapper {
    public static final String DEFAULT_DESCRIPTION = "Shipping Charge";
    private static final Map<String, String> MAP;

    static {
        MAP = LocaleConfig.INSTANCE.getShippingCharge();
    }

    private ShippingChargeMapper() {
    }

    public static String getDescription(String locale) {
        if (isBlank(locale)) {
            return DEFAULT_DESCRIPTION;
        }

        final String description = MAP.get(locale);
        return isBlank(description) ? DEFAULT_DESCRIPTION : description;
    }
}
