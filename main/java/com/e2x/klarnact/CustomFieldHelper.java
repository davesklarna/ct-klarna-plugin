package com.e2x.klarnact;

import com.commercetools.api.models.type.CustomFields;

public class CustomFieldHelper {
    private CustomFieldHelper() {
    }

    public static <T> T getCustomField(CustomFields custom, String fieldName, Class<T> clazz) {
        return custom == null ||
                custom.getFields() == null ||
                custom.getFields().values() == null ? null :
                clazz.cast(custom.getFields().values().get(fieldName));
    }
}
