package com.e2x.klarnact.order;

import com.commercetools.api.models.cart.Cart;
import com.e2x.klarnact.klarna.model.Attachment;
import com.e2x.klarnact.klarna.model.KlarnaCurrency;
import com.e2x.klarnact.klarna.model.order.Order;
import com.e2x.klarnact.klarna.model.order.OrderLine;
import com.e2x.klarnact.mapper.OrderLineMapper;
import com.e2x.klarnact.order.create.CartAndPayment;
import com.e2x.klarnact.payment.CartHelper;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

import static com.e2x.klarnact.klarna.model.order.OrderLine.otherPayment;
import static com.e2x.klarnact.klarna.model.order.OrderLine.shippingCharge;
import static com.e2x.klarnact.mapper.AddressMapper.map;
import static com.e2x.klarnact.mapper.TaxMapper.*;
import static org.apache.commons.lang3.StringUtils.isBlank;

@ApplicationScoped
@AllArgsConstructor
public class OrderToOrderMapper {
    private final OrderLineMapper orderLineMapper;

    public Uni<Order> mapToOrder(com.commercetools.api.models.order.Order in, CartAndPayment cartAndPayment) {
        final var paymentAmountPlanned = cartAndPayment.getPayment().getAmountPlanned();
        final int paymentAmount = paymentAmountPlanned.getCentAmount().intValue();
        final int orderAmount = in.getTotalPrice().getCentAmount().intValue();
        final Cart cart = cartAndPayment.getCart();
        return Uni.createFrom().item(() -> {
            final String cartEmd = CartHelper.getExtraMerchantData(cart);
            final String orderEmd = OrderHelper.getExtraMerchantData(in);
            final String emd = isBlank(orderEmd) ? cartEmd : orderEmd;

            final var builder = Order.builder()
                    .locale(in.getLocale())
                    .purchaseCountry(in.getCountry() == null && in.getBillingAddress() != null
                            ? in.getBillingAddress().getCountry()
                            : in.getCountry()
                    )
                    .purchaseCurrency(KlarnaCurrency.valueOf(paymentAmountPlanned.getCurrencyCode()))
                    .orderAmount(paymentAmount)
                    .merchantReference1(in.getOrderNumber() == null ? in.getId() : in.getOrderNumber())
                    .billingAddress(map(in.getBillingAddress()))
                    .shippingAddress(map(in.getShippingAddress()));

            final List<OrderLine> orderLines = orderLineMapper.fromLineItems(cart.getLocale(), cart.getLineItems());
            if (orderAmount > paymentAmount) {
                orderLines.add(otherPayment(cart.getLocale(), (orderAmount - paymentAmount)));
            }
            if (in.getShippingInfo() != null && in.getShippingInfo().getPrice() != null) {
                orderLines.add(shippingCharge(
                        in.getLocale(),
                        in.getShippingInfo().getPrice().getCentAmount().intValue(),
                        taxRate(cart.getShippingInfo().getTaxRate()),
                        totalTaxAmount(cart.getShippingInfo().getTaxedPrice())
                ));
            }
            orderLines.addAll(orderLineMapper.fromCustomLineItems(in.getLocale(), in.getCustomLineItems()));
            builder.orderLines(orderLines).orderTaxAmount(orderTaxAmount(cart.getTaxedPrice()));

            if (!isBlank(emd)) {
                builder.attachment(Attachment.builder().body(emd).build());
            }

            return builder.build();
        });
    }
}
