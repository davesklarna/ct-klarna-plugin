package com.e2x.klarnact.mapper;

import com.commercetools.api.models.cart.TaxedItemPrice;
import com.commercetools.api.models.cart.TaxedPrice;
import com.commercetools.api.models.tax_category.TaxRate;

public class TaxMapper {
    private TaxMapper() {
    }

    public static Integer orderTaxAmount(TaxedPrice taxedPrice) {
        if (taxedPrice == null || taxedPrice.getTaxPortions() == null) return null;
        return taxedPrice.getTaxPortions().stream()
                .mapToInt(it -> it.getAmount().getCentAmount().intValue())
                .sum();
    }

    public static Integer taxRate(TaxRate taxRate) {
        if (taxRate == null || taxRate.getAmount() == null) return null;
        return (int) Math.round(taxRate.getAmount() * 10000);
    }

    public static Integer totalTaxAmount(TaxedItemPrice taxedItemPrice) {
        if (taxedItemPrice == null || taxedItemPrice.getTotalGross() == null || taxedItemPrice.getTotalNet() == null) {
            return null;
        }
        return (int) (taxedItemPrice.getTotalGross().getCentAmount() - taxedItemPrice.getTotalNet().getCentAmount());
    }
}
