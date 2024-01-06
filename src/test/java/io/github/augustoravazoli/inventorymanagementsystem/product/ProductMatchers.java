package io.github.augustoravazoli.inventorymanagementsystem.product;

import org.hamcrest.Matcher;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

public class ProductMatchers {

    public static Matcher<Product> product(Long id, String name, String category, Integer quantity, String price) {
        return allOf(
                hasProperty("id", is(id)),
                hasProperty("name", is(name)),
                hasProperty("category", hasProperty("name", is(category))),
                hasProperty("quantity", is(quantity)),
                hasProperty("price", is(new BigDecimal(price)))
        );
    }

    public static Matcher<Product> product(String name, String category, Integer quantity, String price) {
        return allOf(
                hasProperty("name", is(name)),
                hasProperty("category", hasProperty("name", is(category))),
                hasProperty("quantity", is(quantity)),
                hasProperty("price", is(new BigDecimal(price)))
        );
    }

    public static Matcher<Product> product(String name, Long categoryId, Integer quantity, String price) {
        return allOf(
                hasProperty("name", is(name)),
                hasProperty("categoryId", is(categoryId)),
                hasProperty("quantity", is(quantity)),
                hasProperty("price", is(new BigDecimal(price)))
        );
    }

    public static Matcher<Product> product() {
        return allOf(
                hasProperty("name", nullValue()),
                hasProperty("categoryId", nullValue()),
                hasProperty("quantity", nullValue()),
                hasProperty("price", nullValue())
        );
    }

}
