package io.github.augustoravazoli.inventorymanagementsystem.customer;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

public class CustomerMatchers {

    public static Matcher<Customer> customer(Long id, String name, String address, String phone) {
        return allOf(
                hasProperty("id", is(id)),
                hasProperty("name", is(name)),
                hasProperty("address", is(address)),
                hasProperty("phone", is(phone))
        );
    }

    public static Matcher<Customer> customer(String name, String address, String phone) {
        return allOf(
                hasProperty("name", is(name)),
                hasProperty("address", is(address)),
                hasProperty("phone", is(phone))
        );
    }

    public static Matcher<Customer> customer() {
        return allOf(
                hasProperty("name", nullValue()),
                hasProperty("address", nullValue()),
                hasProperty("phone", nullValue())
        );
    }

}
