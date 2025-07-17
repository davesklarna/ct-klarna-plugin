package com.e2x.klarnact.order.create;

import com.commercetools.api.models.order.OrderReference;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class OrderRequest {
    private final String action;
    private final OrderReference resource;
}
