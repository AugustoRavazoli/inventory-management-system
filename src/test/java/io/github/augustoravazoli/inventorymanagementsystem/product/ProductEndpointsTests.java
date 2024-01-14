package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.TestApplication;
import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import io.github.augustoravazoli.inventorymanagementsystem.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithUserDetails(value = "user@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class ProductEndpointsTests {

    @Autowired
    private MockMvc client;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private List<Category> categories;

    private Category categoryA;
    private Category categoryB;
    private Category categoryC;

    private User user;

    @BeforeEach
    void setup() {
        user = userRepository.save(new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO"));
        categoryA = categoryRepository.save(new Category("A", user));
        categoryB = categoryRepository.save(new Category("B", user));
        categoryC = categoryRepository.save(new Category("C", user));
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class CreateProductTests {

        @Test
        void createProduct() throws Exception {
            // when
            var result = client.perform(post("/products/create")
                    .param("name", "A")
                    .param("categoryId", categoryA.getId().toString())
                    .param("quantity", "1")
                    .param("price", "1.00")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/products/list")
            );
            var productOptional = productRepository.findByName("A");
            assertThat(productOptional).get()
                    .extracting("name", "category.name", "quantity", "price")
                    .containsExactly("A", "A", 1, new BigDecimal("1.00"));
        }

    }

    @Nested
    class ListProductsTests {

        @Test
        void listProducts() throws Exception {
            // given
            productRepository.saveAll(List.of(
                    new Product("A", categoryA, 1, "1.00"),
                    new Product("B", categoryB, 2, "2.00"),
                    new Product("C", categoryC, 3, "3.00")
            ));
            // when
            var result = client.perform(get("/products/list"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("products", hasSize(3)),
                    view().name("product/product-table")
            );
        }

    }

    @Nested
    class FindProductsTests {

        @Test
        void findProducts() throws Exception {
            // given
            productRepository.saveAll(List.of(
                    new Product("A", categoryA, 1, "1.00"),
                    new Product("Aa", categoryRepository.save(new Category("Aa", user)), 2, "2.00")
            ));
            // when
            var result = client.perform(get("/products/find")
                    .param("name", "A")
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("products", hasSize(2)),
                    view().name("product/product-table")
            );
        }

    }

    @Nested
    class UpdateProductTests {

        @Test
        void updateProduct() throws Exception {
            // given
            var id = productRepository.save(new Product("A", categoryA, 1, "1.00")).getId();
            // when
            var result = client.perform(post("/products/update/{id}", id)
                    .param("name", "B")
                    .param("categoryId", categoryB.getId().toString())
                    .param("quantity", "2")
                    .param("price", "2.00")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/products/list")
            );
            var optionalProduct = productRepository.findById(id);
            assertThat(optionalProduct).get()
                    .extracting("name", "category", "quantity", "price")
                    .usingRecursiveFieldByFieldElementComparatorOnFields("category")
                    .containsExactly("B", categoryB, 1, new BigDecimal("2.00"));
        }

    }

    @Nested
    class DeleteProductTests {

        @Test
        void deleteProduct() throws Exception {
            // given
            var id = productRepository.save(new Product("A", categoryA, 1, "1.00")).getId();
            // when
            var result = client.perform(post("/products/delete/{id}", id)
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/products/list")
            );
            assertThat(productRepository.existsById(id)).isFalse();
            assertThat(categoryRepository.existsById(categoryA.getId())).isTrue();
        }

    }

}
