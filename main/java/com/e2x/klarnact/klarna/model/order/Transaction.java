package com.e2x.klarnact.klarna.model.order;

public interface Transaction {

    String getId();

    String getReference();

    TransactionType type();

    enum TransactionType {
        CAPTURE, REFUND
    }
}
