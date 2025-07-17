package com.e2x.klarnact.mapper;

import com.commercetools.api.models.cart.CustomLineItem;
import com.commercetools.api.models.cart.LineItem;
import com.commercetools.api.models.common.Image;
import com.e2x.klarnact.commercetools.config.CommerceToolsMapperConfig;
import com.e2x.klarnact.commercetools.config.CustomLine;
import com.e2x.klarnact.klarna.model.order.OrderLine;
import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.e2x.klarnact.mapper.TaxMapper.taxRate;
import static com.e2x.klarnact.mapper.TaxMapper.totalTaxAmount;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@ApplicationScoped
@AllArgsConstructor
public class OrderLineMapper {
    private final CommerceToolsMapperConfig commerceToolsMapperConfig;

    public List<OrderLine> fromLineItems(String locale, List<LineItem> lineItems) {
        return IntStream.range(0, lineItems.size())
                .mapToObj(i -> {
                    final LineItem item = lineItems.get(i);
                    final String itemName = item.getName().values().get(locale);
                    return OrderLine.builder()
                            .name(itemName == null ? format("Item %d", i) : itemName)
                            .quantity(item.getQuantity().intValue())
                            .unitPrice(unitPrice(item))
                            .totalAmount(totalAmount(item))
                            .totalDiscountAmount(discountAmount(item))
                            .taxRate(taxRate(item.getTaxRate()))
                            .totalTaxAmount(totalTaxAmount(item.getTaxedPrice()))
                            .imageUrl(imageUrl(item))
                            .build();
                })
                .collect(toList());
    }

    public List<OrderLine> fromCustomLineItems(String locale, List<CustomLineItem> lineItems) {
        if (lineItems == null) return Collections.emptyList();

        final CustomLine clConfig = commerceToolsMapperConfig.customLine();
        final String imageField = clConfig != null && clConfig.imageField().isPresent() ? clConfig.imageField().get() : null;

        return IntStream.range(0, lineItems.size())
                .mapToObj(i -> {
                    final CustomLineItem item = lineItems.get(i);
                    final String itemName = item.getName().values().get(locale);
                    return OrderLine.builder()
                            .name(itemName == null ? format("Item %d", i) : itemName)
                            .quantity(item.getQuantity().intValue())
                            .totalAmount(item.getTotalPrice().getCentAmount().intValue())
                            .unitPrice(unitPrice(item))
                            .taxRate(taxRate(item.getTaxRate()))
                            .totalTaxAmount(totalTaxAmount(item.getTaxedPrice()))
                            .imageUrl(imageUrl(item, imageField))
                            .build();
                })
                .collect(toList());
    }

    static int totalAmount(LineItem item) {
        if (item.getTaxRate() == null || item.getTaxedPrice() == null
                || item.getTaxRate().getIncludedInPrice()) {
            return item.getTotalPrice().getCentAmount().intValue();
        } else {
            return item.getTaxedPrice().getTotalGross().getCentAmount().intValue();
        }
    }

    static int discountAmount(LineItem item) {
        final int cartDiscountAmount = item.getQuantity().intValue() * item.getDiscountedPricePerQuantity().stream()
                .flatMap(dp -> dp.getDiscountedPrice().getIncludedDiscounts().stream())
                .mapToInt(d -> d.getDiscountedAmount().getCentAmount().intValue())
                .sum();

        int itemDiscountAmount = 0;
        if (item.getPrice().getDiscounted() != null) {
            var itemUnitDiscount = item.getPrice().getValue().getCentAmount() - item.getPrice().getDiscounted().getValue().getCentAmount();
            itemDiscountAmount = (int) (itemUnitDiscount * item.getQuantity());
        }

        return cartDiscountAmount + itemDiscountAmount;
    }

    static int unitPrice(LineItem item) {
        if (item.getTaxRate() == null || item.getTaxedPrice() == null
                || item.getTaxRate().getIncludedInPrice()) {
            return item.getPrice().getValue().getCentAmount().intValue();
        } else {
            return (int) (item.getTaxedPrice().getTotalGross().getCentAmount() / item.getQuantity());
        }
    }

    static String imageUrl(LineItem item) {
        if (item == null || item.getVariant() == null || item.getVariant().getImages() == null) return null;
        return item.getVariant().getImages().stream().findFirst().map(Image::getUrl).orElse(null);
    }

    static String imageUrl(CustomLineItem item, String imageField) {
        if (imageField == null || item == null || item.getCustom() == null || item.getCustom().getFields() == null) return null;
        Map<String, Object> customFieldValues = item.getCustom().getFields().values();
        Object imageFieldValue = customFieldValues.getOrDefault(imageField, null);
        return (imageFieldValue instanceof String) ? (String) imageFieldValue : null;
    }

    static int unitPrice(CustomLineItem item) {
        final Long cents = item.getTotalPrice().getCentAmount();
        return (int) (cents / item.getQuantity());
    }
}