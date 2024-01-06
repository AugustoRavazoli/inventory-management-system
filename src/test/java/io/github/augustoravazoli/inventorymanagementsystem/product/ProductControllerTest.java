package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static io.github.augustoravazoli.inventorymanagementsystem.category.CategoryMatchers.category;
import static io.github.augustoravazoli.inventorymanagementsystem.product.ProductMatchers.product;
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

    private final List<Category> categories = List.of(
            new Category(1L, "A"),
            new Category(2L, "B"),
            new Category(3L, "C")
    );

    @Nested
    class CreateProductTests {

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
                            category(1L, "A"),
                            category(2L, "B"),
                            category(3L, "C")
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
                    model().attribute("product", is(product("A", 1L, 1,  "1.00"))),
                    model().attribute("categories", contains(
                            category(1L, "A"),
                            category(2L, "B"),
                            category(3L, "C")
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

    @Nested
    class ListProductsTests {

        @Test
        void listProducts() throws Exception {
            // given
            var products = List.of(
                    new Product("A", new Category("A"), 1, "1.00"),
                    new Product("B", new Category("B"), 2, "2.00"),
                    new Product("C", new Category("C"), 3, "3.00")
            );
            var pageable = PageRequest.of(0, 8, Sort.by("name"));
            when(productService.listProducts(anyInt())).thenReturn(new PageImpl<>(products, pageable, 3));
            // when
            var result = client.perform(get("/products/list"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("products", contains(
                            product("A", "A", 1, "1.00"),
                            product("B", "B", 2, "2.00"),
                            product("C", "C", 3, "3.00")
                    )),
                    model().attribute("currentPage", 1),
                    model().attribute("totalPages", 1),
                    view().name("product/product-table")
            );
            verify(productService, times(1)).listProducts(anyInt());
        }

    }

    @Nested
    class FindProductsTests {

        @Test
        void findProducts() throws Exception {
            // given
            var products = List.of(
                    new Product("A", new Category("A"), 1, "1.00"),
                    new Product("Aa", new Category("Aa"), 2, "2.00")
            );
            when(productService.findProducts(anyString())).thenReturn(products);
            // when
            var result = client.perform(get("/products/find")
                    .param("name", "A")
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("products", contains(
                            product("A", "A", 1, "1.00"),
                            product("Aa", "Aa", 2, "2.00")
                    )),
                    view().name("product/product-table")
            );
            verify(productService, times(1)).findProducts(anyString());
        }

    }

    @Nested
    class UpdateProductTests {

        @Test
        void retrieveUpdateProductPage() throws Exception {
            // given
            var product = new Product(1L, "A", new Category(1L, "A"), 1, new BigDecimal("1.00"));
            when(productService.findProduct(anyLong())).thenReturn(product);
            when(categoryService.listCategories()).thenReturn(categories);
            // when
            var result = client.perform(get("/products/update/{id}", 1));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("product", is(product("A", 1L, 1, "1.00"))),
                    model().attribute("id", 1L),
                    model().attribute("categories", contains(
                            category(1L, "A"),
                            category(2L, "B"),
                            category(3L, "C")
                    )),
                    model().attribute("mode", "update"),
                    view().name("product/product-form")
            );
        }

        @Test
        void updateProduct() throws Exception {
            // when
            var result = client.perform(post("/products/update/{id}", 1)
                    .param("name", "B")
                    .param("categoryId", "2")
                    .param("quantity", "2")
                    .param("price", "2.00")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/products/list")
            );
            verify(productService, times(1)).updateProduct(anyLong(), any(Product.class));
        }

        @Test
        void doNotUpdateProductUsingNameTaken() throws Exception {
            // given
            doThrow(ProductNameTakenException.class).when(productService).updateProduct(anyLong(), any(Product.class));
            when(categoryService.listCategories()).thenReturn(categories);
            // when
            var result = client.perform(post("/products/update/{id}", 1)
                    .param("name", "B")
                    .param("categoryId", "2")
                    .param("quantity", "2")
                    .param("price", "2.00")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("duplicatedName", true),
                    model().attribute("product", is(product("B", 2L, 2, "2.00"))),
                    model().attribute("id", 1L),
                    model().attribute("categories", contains(
                            category(1L, "A"),
                            category(2L, "B"),
                            category(3L, "C")
                    )),
                    model().attribute("mode", "update"),
                    view().name("product/product-form")
            );
            verify(productService, times(1)).updateProduct(anyLong(), any(Product.class));
        }

        @Test
        void doNotUpdateProductUsingBlankFields() throws Exception {
            // when
            var result = client.perform(post("/products/update/{id}", 1)
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

    @Nested
    class DeleteProductTests {

        @Test
        void deleteProduct() throws Exception {
            // when
            var result = client.perform(post("/products/delete/{id}", 1L)
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/products/list")
            );
            verify(productService, times(1)).deleteProduct(anyLong());
        }

    }

}
