package com.e2x.klarnact.klarna.model.order;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Release implements Transaction {
    private final TransactionType type;
    private final String id;
    private final String reference;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public TransactionType type() {
        return type;
    }
}
