package com.e2x.klarnact.order.create;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderSetCustomTypeAction;
import com.commercetools.api.models.order.OrderUpdate;
import com.e2x.klarnact.commercetools.cart.CartServiceImpl;
import com.e2x.klarnact.commercetools.cart.CartServiceMock;
import com.e2x.klarnact.commercetools.order.data.OrderTestData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import static com.e2x.klarnact.CustomFields.Order.KLARNA_ORDER_ID;
import static com.e2x.klarnact.config.ObjectMapperConfig.getObjectMapper;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CreateOrderResourceTestInt {
    public static final String CART_ID = "d384ceef-2b25-48ba-8d1b-ac1e5e11755a";

    @Inject
    ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        QuarkusMock.installMockForType(
                new CartServiceMock(objectMapper),
                CartServiceImpl.class
        );
    }

    @Test
    public void createOrderWhenCartExistsWithPaymentInfo() {
        final ObjectNode root = JsonNodeFactory.instance.objectNode();
        root.put("action", "Create");
        final ObjectNode resource = JsonNodeFactory.instance.objectNode();
        resource.put("typeId", "order");
        final Order order = OrderTestData.getOrder();
        resource.set("obj", getObjectMapper().valueToTree(order));
        root.set("resource", resource);

        RequestSpecification request = given()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(root.toString());

        Response response = request.when()
                .post("/api/v1/orders/create");

        final var res = response.body().as(OrderUpdate.class);

        assertNotNull(res.getActions());
        assertFalse(res.getActions().isEmpty());
        res.getActions().stream().filter(a -> a instanceof OrderSetCustomTypeAction).findFirst().ifPresent(a ->
                assertTrue(((OrderSetCustomTypeAction) a).getFields().values().containsKey(KLARNA_ORDER_ID))
        );
    }
}
