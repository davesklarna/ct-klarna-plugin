package com.e2x.klarnact;

public class CustomFields {
    private CustomFields() {
    }

    public static final class Payment {
        private Payment() {
        }

        public static final String KLARNA_CLIENT_TOKEN = "klarnaClientToken";
        public static final String KLARNA_AUTH_TOKEN = "klarnaAuthToken";
        public static final String ORDER_ID = "orderId";
        public static final String SESSION_ID = "sessionId";
    }

    public static final class Order {
        private Order() {
        }

        public static final String KLARNA_ORDER_ID = "klarnaOrderId";
        public static final String EXTRA_MERCHANT_DATA = "extraMerchantData";
    }
}
