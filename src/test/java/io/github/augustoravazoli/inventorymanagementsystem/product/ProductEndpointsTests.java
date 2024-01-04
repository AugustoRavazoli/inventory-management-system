package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.TestApplication;
import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
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
@WithMockUser
class ProductEndpointsTests {

    @Autowired
    private MockMvc client;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private List<Category> categories;

    private Category categoryA;
    private Category categoryB;
    private Category categoryC;

    @BeforeEach
    void setup() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        categoryA = categoryRepository.save(new Category("A"));
        categoryB = categoryRepository.save(new Category("B"));
        categoryC = categoryRepository.save(new Category("C"));
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
                    .hasFieldOrPropertyWithValue("name", "A")
                    .hasFieldOrPropertyWithValue("category.name", "A")
                    .hasFieldOrPropertyWithValue("quantity", 1)
                    .hasFieldOrPropertyWithValue("price", new BigDecimal("1.00"));
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
