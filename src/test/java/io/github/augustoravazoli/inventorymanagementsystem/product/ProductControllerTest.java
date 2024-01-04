package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryService;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@WithMockUser
class ProductControllerTest {

    @MockBean
    private ProductService productService;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc client;

    @Nested
    class CreateProductTests {

        private final List<Category> categories = List.of(
                new Category(1L, "A"),
                new Category(2L, "B"),
                new Category(3L, "C")
        );

        @Test
        void retrieveCreateProductPage() throws Exception {
            // given
            when(categoryService.listCategories()).thenReturn(categories);
            // when
            var result = client.perform(get("/products/create"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("product", is(product())),
                    model().attribute("categories", contains(
                            category(1, "A"),
                            category(2, "B"),
                            category(3, "C")
                    )),
                    model().attribute("mode", "create"),
                    view().name("product/product-form")
            );
        }

        @Test
        void createProduct() throws Exception {
            // when
            var result = client.perform(post("/products/create")
                    .param("name", "A")
                    .param("categoryId", "1")
                    .param("quantity", "1")
                    .param("price", "1.00")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/products/list")
            );
            verify(productService, times(1)).createProduct(any(Product.class));
        }

        @Test
        void doNotCreateProductWithNameTaken() throws Exception {
            // given
            when(categoryService.listCategories()).thenReturn(categories);
            doThrow(ProductNameTakenException.class).when(productService).createProduct(any(Product.class));
            // when
            var result = client.perform(post("/products/create")
                    .param("name", "A")
                    .param("categoryId", "1")
                    .param("quantity", "1")
                    .param("price", "1.00")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("duplicatedName", true),
                    model().attribute("product", is(
                            product("A", 1L, 1, new BigDecimal("1.00"))
                    )),
                    model().attribute("categories", contains(
                            category(1, "A"),
                            category(2, "B"),
                            category(3, "C")
                    )),
                    model().attribute("mode", "create"),
                    view().name("product/product-form")
            );
            verify(productService, times(1)).createProduct(any(Product.class));
        }

        @Test
        void doNotCreateProductWithBlankFields() throws Exception {
            // when
            var result = client.perform(post("/products/create")
                    .param("name", "")
                    .param("categoryId", "")
                    .param("quantity", "")
                    .param("price", "")
                    .with(csrf())
            );
            // then
            result.andExpect(status().isBadRequest());
        }

    }

    private Matcher<Category> category(long id, String name) {
        return allOf(
                hasProperty("id", is(id)),
                hasProperty("name", is(name))
        );
    }

    private Matcher<Product> product(String name, Long categoryId, Integer quantity, BigDecimal price) {
        return allOf(
                hasProperty("name", is(name)),
                hasProperty("categoryId", is(categoryId)),
                hasProperty("quantity", is(quantity)),
                hasProperty("price", is(price))
        );
    }

    private Matcher<Product> product() {
        return product(null, null, null, null);
    }

}
