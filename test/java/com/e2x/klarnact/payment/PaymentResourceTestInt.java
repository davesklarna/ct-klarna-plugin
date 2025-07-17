package com.e2x.klarnact.payment;

import com.commercetools.api.models.customer.CustomerReference;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.PaymentReference;
import com.commercetools.api.models.payment.PaymentSetCustomTypeAction;
import com.commercetools.api.models.payment.PaymentUpdate;
import com.commercetools.api.models.type.CustomFields;
import com.commercetools.api.models.type.FieldContainer;
import com.e2x.klarnact.commercetools.cart.CartServiceImpl;
import com.e2x.klarnact.commercetools.cart.CartServiceMock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.e2x.klarnact.CustomFields.Payment.KLARNA_CLIENT_TOKEN;
import static com.e2x.klarnact.CustomFields.Payment.SESSION_ID;
import static com.e2x.klarnact.PaymentProducer.getPayment;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class PaymentResourceTestInt {

    public static final String CART_ANONYMOUS_ID = "1e291d10-4bfe-4a7e-984f-4943c1d47d63";
    public static final String CART_CUSTOMER_ID = "22463892-a747-4145-b6c5-1f0b74eccfa4";
    public static final int CART_CENT_AMOUNT = 149250;
    private static final String CART_CURRENCY_CODE = "EUR";

    public static final String ACTION_ACTION = "setCustomType";
    public static final String ACTION_NAME = KLARNA_CLIENT_TOKEN;

    public static final String CLIENT_TOKEN = UUID.randomUUID().toString();

    public static final String ORDER_ID = "d384ceef-2b25-48ba-8d1b-ac1e5e11755b";

    @Inject
    ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        QuarkusMock.installMockForType(new CartServiceMock(objectMapper), CartServiceImpl.class);
    }

    @Test
    public void createPaymentSuccessfullyForAnonymousId() {

        RequestSpecification request = given()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\n" +
                        "    \"action\": \"Create\",\n" +
                        "    \"resource\": {\n" +
                        "        \"typeId\": \"payment\",\n" +
                        "        \"obj\": {\n" +
                        "            \"amountPlanned\": {\n" +
                        "                \"centAmount\": " + CART_CENT_AMOUNT + ",\n" +
                        "                \"currencyCode\": \"" + CART_CURRENCY_CODE + "\"\n" +
                        "            },\n" +
                        "            \"paymentMethodInfo\": {\n" +
                        "                \"paymentInterface\": \"Klarna\"" +
                        "            },\n" +
                        "            \"anonymousId\": \"" + CART_ANONYMOUS_ID + "\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");

        Response response = request.when()
                .post("/api/v1/payments/create");

        final var res = response.body().as(PaymentUpdate.class);
        final PaymentSetCustomTypeAction action = (PaymentSetCustomTypeAction) res.getActions().stream()
                .filter(a -> a instanceof PaymentSetCustomTypeAction).findFirst().orElse(null);

        assertNotNull(action);
        assertEquals(ACTION_ACTION, action.getAction());
        assertTrue(action.getFields().values().containsKey(ACTION_NAME));
        assertEquals(CLIENT_TOKEN, action.getFields().values().get(ACTION_NAME));
    }

    @Test
    public void createPaymentSuccessfullyForCustomerId() {

        RequestSpecification request = given()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\n" +
                        "    \"action\": \"Create\",\n" +
                        "    \"resource\": {\n" +
                        "        \"typeId\": \"payment\",\n" +
                        "        \"obj\": {\n" +
                        "            \"amountPlanned\": {\n" +
                        "               \"centAmount\": " + CART_CENT_AMOUNT + ",\n" +
                        "                \"currencyCode\": \"" + CART_CURRENCY_CODE + "\"\n" +
                        "            },\n" +
                        "            \"paymentMethodInfo\": {\n" +
                        "                \"paymentInterface\": \"Klarna\"" +
                        "            },\n" +
                        "            \"customer\": {\n" +
                        "                \"typeId\": \"customer\",\n" +
                        "                \"id\": \"" + CART_CUSTOMER_ID + "\"\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");

        Response response = request.when()
                .post("/api/v1/payments/create");

        final var res = response.body().as(PaymentUpdate.class);
        final PaymentSetCustomTypeAction action = (PaymentSetCustomTypeAction) res.getActions().stream()
                .filter(a -> a instanceof PaymentSetCustomTypeAction).findFirst().orElse(null);

        assertNotNull(action);
        assertEquals(ACTION_ACTION, action.getAction());
        assertTrue(action.getFields().values().containsKey(ACTION_NAME));
        assertEquals(CLIENT_TOKEN, action.getFields().values().get(ACTION_NAME));
    }

    @Test
    public void cartNotFoundExceptionReturned() {

        final String notFoundId = UUID.randomUUID().toString();

        RequestSpecification request = given()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\n" +
                        "    \"action\": \"Create\",\n" +
                        "    \"resource\": {\n" +
                        "        \"typeId\": \"payment\",\n" +
                        "        \"obj\": {\n" +
                        "            \"amountPlanned\": {\n" +
                        "               \"centAmount\": " + CART_CENT_AMOUNT + ",\n" +
                        "                \"currencyCode\": \"" + CART_CURRENCY_CODE + "\"\n" +
                        "            },\n" +
                        "            \"paymentMethodInfo\": {\n" +
                        "                \"paymentInterface\": \"Klarna\"" +
                        "            },\n" +
                        "            \"customer\": {\n" +
                        "                \"typeId\": \"customer\",\n" +
                        "                \"id\": \"" + notFoundId + "\"\n" +
                        "                }\n" +
                        "            }\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");

        Response response = request.when()
                .post("/api/v1/payments/create");

        final var res = response.body().as(JsonNode.class);

        assertNotNull(res);
        assertEquals(404, res.get("statusCode").asInt());
        assertEquals("Not Found", res.get("statusDescription").asText());
        assertEquals(format("Cart Not Found with customerId of %s", notFoundId), res.get("message").asText());
    }

    @Test
    public void paymentNotPopulatedCorrectly() {

        RequestSpecification request = given()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\n" +
                        "    \"action\": \"Create\",\n" +
                        "    \"resource\": {\n" +
                        "        \"typeId\": \"payment\",\n" +
                        "            \"anonymousId\": \"" + CART_ANONYMOUS_ID + "\"\n" +
                        "        }\n" +
                        "    }\n" +
                        "}");

        Response response = request.when()
                .post("/api/v1/payments/create");

        final var res = response.body().as(JsonNode.class);

        assertNotNull(res);
        assertEquals(400, res.get("statusCode").asInt());
        assertEquals("Bad Request", res.get("statusDescription").asText());
        assertEquals("Payment Reference must not be null", res.get("message").asText());
    }

    @Test
    public void orderNotFoundExceptionReturned() throws IOException {
        final String notFoundId = UUID.randomUUID().toString();
        final String sessionId = UUID.randomUUID().toString();
        final Payment payment = Payment.builder(requireNonNull(getPayment()))
                .customer(CustomerReference.builder()
                        .id(CART_CUSTOMER_ID)
                        .build())
                .custom(CustomFields.builder()
                        .fields(FieldContainer.builder()
                                .values(Map.of(
                                        SESSION_ID, sessionId,
                                        com.e2x.klarnact.CustomFields.Payment.ORDER_ID, notFoundId))
                                .build())
                        .build())
                .build();

        final PaymentRequest req = new PaymentRequest(
                "Update",
                PaymentReference.builder()
                        .id(payment.getId())
                        .obj(payment)
                        .build()
        );

        RequestSpecification request = given()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(req));

        Response response = request.when()
                .post("/api/v1/payments/update");

        final var res = response.body().as(JsonNode.class);

        assertNotNull(res);
        assertEquals(404, res.get("statusCode").asInt());
        assertEquals("Not Found", res.get("statusDescription").asText());
        assertEquals(format("Order Not Found with id of %s", notFoundId), res.get("message").asText());
    }
}
