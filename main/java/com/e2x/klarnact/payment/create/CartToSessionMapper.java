package com.e2x.klarnact.payment.create;

import com.commercetools.api.models.cart.Cart;
import com.commercetools.api.models.payment.Payment;
import com.e2x.klarnact.klarna.model.Attachment;
import com.e2x.klarnact.klarna.model.KlarnaCurrency;
import com.e2x.klarnact.klarna.model.order.OrderLine;
import com.e2x.klarnact.klarna.model.payment.CreditSession;
import com.e2x.klarnact.mapper.OrderLineMapper;
import io.smallrye.mutiny.Uni;
import lombok.AllArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

import static com.e2x.klarnact.klarna.model.order.OrderLine.otherPayment;
import static com.e2x.klarnact.klarna.model.order.OrderLine.shippingCharge;
import static com.e2x.klarnact.mapper.TaxMapper.*;
import static com.e2x.klarnact.payment.CartHelper.getExtraMerchantData;
import static org.apache.commons.lang3.StringUtils.isBlank;

@ApplicationScoped
@AllArgsConstructor
public class CartToSessionMapper {
    private final OrderLineMapper orderLineMapper;

    public Uni<CreditSession> mapToSession(Cart cart, Payment payment) {
        final String emd = getExtraMerchantData(cart);
        return Uni.createFrom().item(() -> {
                    final int cartAmount = cart.getTotalPrice().getCentAmount().intValue();
                    final int paymentAmount = payment.getAmountPlanned().getCentAmount().intValue();
                    final var builder = CreditSession.builder()
                            .locale(cart.getLocale())
                            .purchaseCountry(cart.getCountry())
                            .purchaseCurrency(KlarnaCurrency.valueOf(payment.getAmountPlanned().getCurrencyCode()))
                            .orderAmount(paymentAmount)
                            .merchantReference2(cart.getId());

                    if (!isBlank(emd)) {
                        builder.attachment(Attachment.builder().body(emd).build());
                    }

                    final List<OrderLine> orderLines = orderLineMapper.fromLineItems(cart.getLocale(), cart.getLineItems());
                    if (cartAmount > paymentAmount) {
                        orderLines.add(otherPayment(cart.getLocale(), (cartAmount - paymentAmount)));
                    }
                    if (cart.getShippingInfo() != null && cart.getShippingInfo().getPrice() != null) {
                        orderLines.add(shippingCharge(
                                cart.getLocale(),
                                cart.getShippingInfo().getPrice().getCentAmount().intValue(),
                                taxRate(cart.getShippingInfo().getTaxRate()),
                                totalTaxAmount(cart.getShippingInfo().getTaxedPrice())
                        ));
                    }
                    orderLines.addAll(orderLineMapper.fromCustomLineItems(cart.getLocale(), cart.getCustomLineItems()));
                    builder.orderLines(orderLines)
                            .orderTaxAmount(orderTaxAmount(cart.getTaxedPrice()));

                    return builder.build();
                }
        );
    }
}
