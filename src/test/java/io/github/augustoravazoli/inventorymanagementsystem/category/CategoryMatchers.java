package io.github.augustoravazoli.inventorymanagementsystem.category;

import org.hamcrest.Matcher;

import static org.hamcrest.Matchers.*;

public class CategoryMatchers {

    public static Matcher<Category> category(Long id, String name) {
        return allOf(
                hasProperty("id", is(id)),
                hasProperty("name", is(name))
        );
    }

    public static Matcher<Category> category(String name) {
        return hasProperty("name", is(name));
    }

    public static Matcher<Category> category() {
        return hasProperty("name", nullValue());
    }

}
