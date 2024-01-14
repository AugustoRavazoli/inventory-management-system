package io.github.augustoravazoli.inventorymanagementsystem.category;

import io.github.augustoravazoli.inventorymanagementsystem.TestApplication;
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
class CategoryEndpointsTests {

    @Autowired
    private MockMvc client;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        user = userRepository.save(new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO"));
    }

    @AfterEach
    void tearDown() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class CreateCategoryTests {

        @Test
        void createCategory() throws Exception {
            // when
            var result = client.perform(post("/categories/create")
                    .param("name", "A")
                    .with(csrf()));
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/categories/list")
            );
            var categoryOptional = categoryRepository.findByNameAndOwner("A", user);
            assertThat(categoryOptional).get().extracting("name", "owner.email")
                    .containsExactly("A", "user@email.com");
        }

    }

    @Nested
    class ListCategoriesTests {

        @Test
        void listCategories() throws Exception {
            // given
            categoryRepository.saveAll(List.of(
                    new Category("A", user),
                    new Category("B", user),
                    new Category("C", user)
            ));
            // when
            var result = client.perform(get("/categories/list"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("categories", hasSize(3)),
                    view().name("category/category-table")
            );
        }

    }

    @Nested
    class FindCategoriesTests {

        @Test
        void findCategories() throws Exception {
            // given
            categoryRepository.saveAll(List.of(
                    new Category("A", user),
                    new Category("Aa", user)
            ));
            // when
            var result = client.perform(get("/categories/find")
                    .param("name", "A")
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("categories", hasSize(2)),
                    view().name("category/category-table")
            );
        }

    }

    @Nested
    class UpdateCategoryTests {

        @Test
        void updateCategory() throws Exception {
            // given
            var id = categoryRepository.save(new Category("A", user)).getId();
            // when
            var result = client.perform(post("/categories/update/{id}", id)
                    .param("name", "B")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/categories/list")
            );
            var categoryOptional = categoryRepository.findById(id);
            assertThat(categoryOptional).get().extracting("name", "owner.email")
                    .containsExactly("B", "user@email.com");
        }

    }

    @Nested
    class DeleteCategoryTests {

        @Test
        void deleteCategory() throws Exception {
            // given
            var id = categoryRepository.save(new Category("A", user)).getId();
            // when
            var result = client.perform(post("/categories/delete/{id}", id)
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/categories/list")
            );
            assertThat(categoryRepository.existsById(id)).isFalse();
        }

    }

}
