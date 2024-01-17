package io.github.augustoravazoli.inventorymanagementsystem.order;

import org.hamcrest.Matcher;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

public class OrderMatchers {

    public static Matcher<OrderForm> order() {
        return allOf(
                hasProperty("status", nullValue()),
                hasProperty("customerId", nullValue()),
                hasProperty("items", nullValue())
        );
    }

    public static Matcher<OrderForm> order(String status, Long customerId, Matcher<Iterable<? extends OrderItemForm>> itemsMatcher) {
        return allOf(
                hasProperty("status", is(OrderStatus.valueOf(status))),
                hasProperty("customerId", is(customerId)),
                hasProperty("items", itemsMatcher)
        );
    }

    public static Matcher<OrderForm> order(String customer, LocalDate date, Integer quantity, String price) {
        return allOf(
                hasProperty("customer", hasProperty("name", is(customer))),
                hasProperty("date", is(date)),
                hasProperty("quantity", is(quantity)),
                hasProperty("amount", is(new BigDecimal(price)))
        );
    }

    public static Matcher<OrderItemForm> item(Integer quantity, Long productId) {
        return allOf(
                hasProperty("quantity", is(quantity)),
                hasProperty("productId", is(productId))
        );
    }

}
