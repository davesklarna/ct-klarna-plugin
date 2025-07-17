package com.e2x.klarnact.mapper;

import com.commercetools.api.models.cart.*;
import com.commercetools.api.models.common.Image;
import com.commercetools.api.models.common.Price;
import com.e2x.klarnact.config.CommerceToolsMapperConfigTest;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderLineMapperTest {
    @Nested
    class DiscountAmount {

        @Test
        void shouldCalculateDiscountAmountWhenOnlyCartDiscountPresent() {
            var lineItemMock = mock(LineItem.class);
            var discountedLineItemPriceForQuantityMock1 = mock(DiscountedLineItemPriceForQuantity.class);
            var discountedLineItemPriceForQuantityMock2 = mock(DiscountedLineItemPriceForQuantity.class);
            var discountedLineItemPriceMock1 = mock(DiscountedLineItemPrice.class);
            var discountedLineItemPriceMock2 = mock(DiscountedLineItemPrice.class);
            var discountedLineItemPortionMock1 = mock(DiscountedLineItemPortion.class, RETURNS_DEEP_STUBS);
            var discountedLineItemPortionMock2 = mock(DiscountedLineItemPortion.class, RETURNS_DEEP_STUBS);
            var discountedLineItemPortionMock3 = mock(DiscountedLineItemPortion.class, RETURNS_DEEP_STUBS);
            var priceMock = mock(Price.class);

            when(lineItemMock.getDiscountedPricePerQuantity()).thenReturn(List.of(discountedLineItemPriceForQuantityMock1, discountedLineItemPriceForQuantityMock2));
            when(discountedLineItemPriceForQuantityMock1.getDiscountedPrice()).thenReturn(discountedLineItemPriceMock1);
            when(discountedLineItemPriceForQuantityMock2.getDiscountedPrice()).thenReturn(discountedLineItemPriceMock2);
            when(discountedLineItemPriceMock1.getIncludedDiscounts()).thenReturn(List.of(discountedLineItemPortionMock1));
            when(discountedLineItemPriceMock2.getIncludedDiscounts()).thenReturn(List.of(discountedLineItemPortionMock2, discountedLineItemPortionMock3));
            when(discountedLineItemPortionMock1.getDiscountedAmount().getCentAmount()).thenReturn(1000L);
            when(discountedLineItemPortionMock2.getDiscountedAmount().getCentAmount()).thenReturn(2000L);
            when(discountedLineItemPortionMock3.getDiscountedAmount().getCentAmount()).thenReturn(3500L);
            when(lineItemMock.getPrice()).thenReturn(priceMock);
            when(lineItemMock.getQuantity()).thenReturn(3L);

            var result = OrderLineMapper.discountAmount(lineItemMock);

            assertEquals(19500, result);
        }

        @Test
        void shouldCalculateDiscountAmountWhenBothCartAndItemDiscountPresent() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);
            var discountedLineItemPriceForQuantityMock1 = mock(DiscountedLineItemPriceForQuantity.class);
            var discountedLineItemPriceForQuantityMock2 = mock(DiscountedLineItemPriceForQuantity.class);
            var discountedLineItemPriceMock1 = mock(DiscountedLineItemPrice.class);
            var discountedLineItemPriceMock2 = mock(DiscountedLineItemPrice.class);
            var discountedLineItemPortionMock1 = mock(DiscountedLineItemPortion.class, RETURNS_DEEP_STUBS);
            var discountedLineItemPortionMock2 = mock(DiscountedLineItemPortion.class, RETURNS_DEEP_STUBS);
            var discountedLineItemPortionMock3 = mock(DiscountedLineItemPortion.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getDiscountedPricePerQuantity()).thenReturn(List.of(discountedLineItemPriceForQuantityMock1, discountedLineItemPriceForQuantityMock2));
            when(discountedLineItemPriceForQuantityMock1.getDiscountedPrice()).thenReturn(discountedLineItemPriceMock1);
            when(discountedLineItemPriceForQuantityMock2.getDiscountedPrice()).thenReturn(discountedLineItemPriceMock2);
            when(discountedLineItemPriceMock1.getIncludedDiscounts()).thenReturn(List.of(discountedLineItemPortionMock1));
            when(discountedLineItemPriceMock2.getIncludedDiscounts()).thenReturn(List.of(discountedLineItemPortionMock2, discountedLineItemPortionMock3));
            when(discountedLineItemPortionMock1.getDiscountedAmount().getCentAmount()).thenReturn(1000L);
            when(discountedLineItemPortionMock2.getDiscountedAmount().getCentAmount()).thenReturn(2000L);
            when(discountedLineItemPortionMock3.getDiscountedAmount().getCentAmount()).thenReturn(3500L);

            when(lineItemMock.getPrice().getValue().getCentAmount()).thenReturn(600L);
            when(lineItemMock.getPrice().getDiscounted().getValue().getCentAmount()).thenReturn(200L);
            when(lineItemMock.getQuantity()).thenReturn(3L);

            var result = OrderLineMapper.discountAmount(lineItemMock);

            assertEquals(20700, result);
        }

        @Test
        void shouldCalculateDiscountAmountWhenOnlyItemDiscountPresent() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getPrice().getValue().getCentAmount()).thenReturn(600L);
            when(lineItemMock.getPrice().getDiscounted().getValue().getCentAmount()).thenReturn(200L);
            when(lineItemMock.getQuantity()).thenReturn(3L);

            var result = OrderLineMapper.discountAmount(lineItemMock);

            assertEquals(1200, result);
        }
    }

    @Nested
    class UnitPrice {

        @Test
        void shouldReturnItemPriceWhenTaxRateNull() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getTaxRate()).thenReturn(null);
            when(lineItemMock.getPrice().getValue().getCentAmount()).thenReturn(400L);

            var result = OrderLineMapper.unitPrice(lineItemMock);

            assertEquals(400, result);
        }

        @Test
        void shouldReturnItemPriceWhenTaxedPriceNull() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getTaxedPrice()).thenReturn(null);
            when(lineItemMock.getPrice().getValue().getCentAmount()).thenReturn(500L);

            var result = OrderLineMapper.unitPrice(lineItemMock);

            assertEquals(500, result);
        }

        @Test
        void shouldReturnItemPriceWhenTaxIncludedInPrice() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getTaxRate().getIncludedInPrice()).thenReturn(true);
            when(lineItemMock.getPrice().getValue().getCentAmount()).thenReturn(600L);

            var result = OrderLineMapper.unitPrice(lineItemMock);

            assertEquals(600, result);
        }

        @Test
        void shouldReturnGrossDividedByQuantity() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getTaxRate().getIncludedInPrice()).thenReturn(false);
            when(lineItemMock.getTaxedPrice().getTotalGross().getCentAmount()).thenReturn(600L);
            when(lineItemMock.getQuantity()).thenReturn(6L);

            var result = OrderLineMapper.unitPrice(lineItemMock);

            assertEquals(100, result);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class ImageUrl {
        //Line Item Tests

        @Test
        void shouldReturnNullForNullItem() {
            var result = OrderLineMapper.imageUrl(null);
            assertNull(result);
        }

        @Test
        void shouldReturnWhenItemMissingVariant() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getVariant()).thenReturn(null);

            var result = OrderLineMapper.imageUrl(lineItemMock);

            assertNull(result);
        }

        @Test
        void shouldReturnWhenVariantMissingImages() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getVariant().getImages()).thenReturn(null);

            var result = OrderLineMapper.imageUrl(lineItemMock);

            assertNull(result);
        }

        @Test
        void shouldReturnFirstVariantImage() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);
            var imageMock = mock(Image.class);

            when(lineItemMock.getVariant().getImages()).thenReturn(List.of(imageMock));
            when(imageMock.getUrl()).thenReturn("image url");

            var result = OrderLineMapper.imageUrl(lineItemMock);

            assertEquals("image url", result);
        }

        //Custom Line Item Tests

        @Test
        void shouldReturnNullForNullCustomLineItem() {
            var result = OrderLineMapper.imageUrl(null, "testing");
            assertNull(result);
        }

        @Test
        void shouldReturnNullForNullImageField() {
            var customLineItemMock = mock(CustomLineItem.class, RETURNS_DEEP_STUBS);

            when (customLineItemMock.getCustom().getFields().values()).thenReturn(
                    Map.of("image-field-test", "image url", "some-other-field", "value"));

            var result = OrderLineMapper.imageUrl(customLineItemMock, null);
            assertNull(result);
        }

        @Test
        void shouldReturnNullWhenItemMissingCustom() {
            var customLineItemMock = mock(CustomLineItem.class, RETURNS_DEEP_STUBS);

            when (customLineItemMock.getCustom()).thenReturn(null);

            var result = OrderLineMapper.imageUrl(customLineItemMock, "image-field-test");

            assertNull(result);
        }

        @Test
        void shouldReturnNullWhenCustomMissingFields() {
            var customLineItemMock = mock(CustomLineItem.class, RETURNS_DEEP_STUBS);

            when (customLineItemMock.getCustom().getFields()).thenReturn(null);

            var result = OrderLineMapper.imageUrl(customLineItemMock, "image-field-test");

            assertNull(result);
        }

        @Test
        void shouldReturnNullWhenFieldValueIsNotString() {
            var customLineItemMock = mock(CustomLineItem.class, RETURNS_DEEP_STUBS);

            when(customLineItemMock.getCustom().getFields().values()).thenReturn(Map.of("image-field-test", LocalDateTime.now()));

            var result = OrderLineMapper.imageUrl(customLineItemMock, "image-field-test");

            assertNull(result);
        }

        @Test
        void shouldReturnImageWithKey(){
            var customLineItemMock = mock(CustomLineItem.class, RETURNS_DEEP_STUBS);

            when (customLineItemMock.getCustom().getFields().values()).thenReturn(
                    Map.of("image-field-test", "image url", "some-other-field", "value"));

            var result = OrderLineMapper.imageUrl(customLineItemMock, "image-field-test");

            assertEquals("image url", result);
        }

    }

    @Nested
    class UnitPriceForCustomItem {

        @Test
        void shouldReturnTotalPriceDividedByQuantity() {
            var customLineItemMock = mock(CustomLineItem.class, RETURNS_DEEP_STUBS);

            when(customLineItemMock.getTotalPrice().getCentAmount()).thenReturn(400L);
            when(customLineItemMock.getQuantity()).thenReturn(2L);

            var result = OrderLineMapper.unitPrice(customLineItemMock);

            assertEquals(200, result);
        }
    }

    @Nested
    class TotalAmount {

        @Test
        void shouldReturnItemPriceWhenTaxRateNull() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getTaxRate()).thenReturn(null);
            when(lineItemMock.getTotalPrice().getCentAmount()).thenReturn(400L);

            var result = OrderLineMapper.totalAmount(lineItemMock);

            assertEquals(400, result);
        }

        @Test
        void shouldReturnItemPriceWhenTaxedPriceNull() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getTaxedPrice()).thenReturn(null);
            when(lineItemMock.getTotalPrice().getCentAmount()).thenReturn(500L);

            var result = OrderLineMapper.totalAmount(lineItemMock);

            assertEquals(500, result);
        }

        @Test
        void shouldReturnItemPriceWhenTaxIncludedInPrice() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getTaxRate().getIncludedInPrice()).thenReturn(true);
            when(lineItemMock.getTotalPrice().getCentAmount()).thenReturn(600L);

            var result = OrderLineMapper.totalAmount(lineItemMock);

            assertEquals(600, result);
        }

        @Test
        void shouldReturnGross() {
            var lineItemMock = mock(LineItem.class, RETURNS_DEEP_STUBS);

            when(lineItemMock.getTaxRate().getIncludedInPrice()).thenReturn(false);
            when(lineItemMock.getTaxedPrice().getTotalGross().getCentAmount()).thenReturn(600L);
            when(lineItemMock.getQuantity()).thenReturn(6L);

            var result = OrderLineMapper.totalAmount(lineItemMock);

            assertEquals(600, result);
        }
    }

    @Nested
    class FromLineItems {

        @Test
        void shouldMapLineItems() {
            var orderLineMapper = new OrderLineMapper(new CommerceToolsMapperConfigTest());
            var lineItemMock1 = mock(LineItem.class, RETURNS_DEEP_STUBS);
            var lineItemMock2 = mock(LineItem.class, RETURNS_DEEP_STUBS);
            var lineItemMock3 = mock(LineItem.class, RETURNS_DEEP_STUBS);
            var imageMock = mock(Image.class);

            when(lineItemMock1.getName().values()).thenReturn(Map.of("en", "en name", "fr", "fr name"));
            when(lineItemMock1.getQuantity()).thenReturn(1L);
            when(lineItemMock1.getTaxRate().getIncludedInPrice()).thenReturn(false);
            when(lineItemMock1.getTaxedPrice().getTotalGross().getCentAmount()).thenReturn(600L);
            when(lineItemMock1.getTaxedPrice().getTotalNet().getCentAmount()).thenReturn(500L);
            when(lineItemMock1.getTaxRate().getAmount()).thenReturn(0.2);

            when(lineItemMock2.getName().values()).thenReturn(Map.of("fr", "fr name 2"));
            when(lineItemMock2.getQuantity()).thenReturn(4L);
            when(lineItemMock2.getTaxedPrice().getTotalGross().getCentAmount()).thenReturn(440L);
            when(lineItemMock2.getTaxedPrice().getTotalNet().getCentAmount()).thenReturn(352L);
            when(lineItemMock2.getPrice().getValue().getCentAmount()).thenReturn(110L);
            when(lineItemMock2.getPrice().getDiscounted().getValue().getCentAmount()).thenReturn(100L);
            when(lineItemMock2.getTaxRate().getAmount()).thenReturn(0.25);

            when(lineItemMock3.getName().values()).thenReturn(Map.of());
            when(lineItemMock3.getQuantity()).thenReturn(9L);
            when(lineItemMock3.getTaxedPrice().getTotalGross().getCentAmount()).thenReturn(945L);
            when(lineItemMock3.getTaxedPrice().getTotalNet().getCentAmount()).thenReturn(859L);
            when(lineItemMock3.getTaxRate().getAmount()).thenReturn(0.1);
            when(lineItemMock3.getVariant().getImages()).thenReturn(List.of(imageMock));
            when(imageMock.getUrl()).thenReturn("item 3 image url");

            var result = orderLineMapper.fromLineItems("en", List.of(lineItemMock1, lineItemMock2, lineItemMock3));

            assertEquals(3, result.size());

            assertEquals("en name", result.get(0).getName());
            assertEquals(1, result.get(0).getQuantity());
            assertEquals(600, result.get(0).getUnitPrice());
            assertEquals(600, result.get(0).getTotalAmount());
            assertEquals(0, result.get(0).getTotalDiscountAmount());
            assertEquals(2000, result.get(0).getTaxRate());
            assertEquals(100, result.get(0).getTotalTaxAmount());
            assertNull(result.get(0).getImageUrl());

            assertEquals("Item 1", result.get(1).getName());
            assertEquals(4, result.get(1).getQuantity());
            assertEquals(110, result.get(1).getUnitPrice());
            assertEquals(440, result.get(1).getTotalAmount());
            assertEquals(40, result.get(1).getTotalDiscountAmount());
            assertEquals(2500, result.get(1).getTaxRate());
            assertEquals(88, result.get(1).getTotalTaxAmount());
            assertNull(result.get(1).getImageUrl());

            assertEquals("Item 2", result.get(2).getName());
            assertEquals(9, result.get(2).getQuantity());
            assertEquals(105, result.get(2).getUnitPrice());
            assertEquals(945, result.get(2).getTotalAmount());
            assertEquals(0, result.get(2).getTotalDiscountAmount());
            assertEquals(1000, result.get(2).getTaxRate());
            assertEquals(86, result.get(2).getTotalTaxAmount());
            assertEquals("item 3 image url", result.get(2).getImageUrl());
        }
    }

    @Nested
    class FromCustomLineItems {
        @Test
        void shouldMapCustomLineItems() {
            var orderLineMapper = new OrderLineMapper(new CommerceToolsMapperConfigTest());
            var customLineItemMock1 = mock(CustomLineItem.class, RETURNS_DEEP_STUBS);
            var customLineItemMock2 = mock(CustomLineItem.class, RETURNS_DEEP_STUBS);
            var customLineItemMock3 = mock(CustomLineItem.class, RETURNS_DEEP_STUBS);

            when(customLineItemMock1.getName().values()).thenReturn(Map.of("en", "en name", "fr", "fr name"));
            when(customLineItemMock1.getQuantity()).thenReturn(1L);
            when(customLineItemMock1.getTaxRate().getIncludedInPrice()).thenReturn(false);
            when(customLineItemMock1.getTotalPrice().getCentAmount()).thenReturn(600L);
            when(customLineItemMock1.getTaxedPrice().getTotalGross().getCentAmount()).thenReturn(600L);
            when(customLineItemMock1.getTaxedPrice().getTotalNet().getCentAmount()).thenReturn(500L);
            when(customLineItemMock1.getTaxRate().getAmount()).thenReturn(0.2);

            when(customLineItemMock2.getName().values()).thenReturn(Map.of("fr", "fr name 2"));
            when(customLineItemMock2.getQuantity()).thenReturn(4L);
            when(customLineItemMock2.getTotalPrice().getCentAmount()).thenReturn(440L);
            when(customLineItemMock2.getTaxedPrice().getTotalGross().getCentAmount()).thenReturn(440L);
            when(customLineItemMock2.getTaxedPrice().getTotalNet().getCentAmount()).thenReturn(352L);
            when(customLineItemMock2.getTaxRate().getAmount()).thenReturn(0.25);
            when(customLineItemMock2.getCustom().getFields().values()).thenReturn(Map.of("not-an-image-url", "value"));

            when(customLineItemMock3.getName().values()).thenReturn(Map.of());
            when(customLineItemMock3.getQuantity()).thenReturn(9L);
            when(customLineItemMock3.getTotalPrice().getCentAmount()).thenReturn(945L);
            when(customLineItemMock3.getTaxedPrice().getTotalGross().getCentAmount()).thenReturn(945L);
            when(customLineItemMock3.getTaxedPrice().getTotalNet().getCentAmount()).thenReturn(859L);
            when(customLineItemMock3.getTaxRate().getAmount()).thenReturn(0.1);
            when(customLineItemMock3.getCustom().getFields().values()).thenReturn(Map.of("image-field-test", "image url"));

            var result = orderLineMapper.fromCustomLineItems("fr", List.of(customLineItemMock1, customLineItemMock2, customLineItemMock3));

            assertEquals(3, result.size());

            assertEquals("fr name", result.get(0).getName());
            assertEquals(1, result.get(0).getQuantity());
            assertEquals(600, result.get(0).getUnitPrice());
            assertEquals(600, result.get(0).getTotalAmount());
            assertEquals(2000, result.get(0).getTaxRate());
            assertEquals(100, result.get(0).getTotalTaxAmount());
            assertNull(result.get(0).getImageUrl());

            assertEquals("fr name 2", result.get(1).getName());
            assertEquals(4, result.get(1).getQuantity());
            assertEquals(110, result.get(1).getUnitPrice());
            assertEquals(440, result.get(1).getTotalAmount());
            assertEquals(2500, result.get(1).getTaxRate());
            assertEquals(88, result.get(1).getTotalTaxAmount());
            assertNull(result.get(1).getImageUrl());

            assertEquals("Item 2", result.get(2).getName());
            assertEquals(9, result.get(2).getQuantity());
            assertEquals(105, result.get(2).getUnitPrice());
            assertEquals(945, result.get(2).getTotalAmount());
            assertEquals(1000, result.get(2).getTaxRate());
            assertEquals(86, result.get(2).getTotalTaxAmount());
            assertEquals("image url", result.get(2).getImageUrl());
        }
    }
}
