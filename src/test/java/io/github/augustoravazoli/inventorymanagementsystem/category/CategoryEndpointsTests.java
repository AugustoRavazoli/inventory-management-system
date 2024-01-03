package io.github.augustoravazoli.inventorymanagementsystem.category;

import io.github.augustoravazoli.inventorymanagementsystem.TestApplication;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser
class CategoryEndpointsTests {

    @Autowired
    private MockMvc client;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setup() {
        categoryRepository.deleteAll();
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
            var categoryOptional = categoryRepository.findByName("A");
            assertThat(categoryOptional).get().hasFieldOrPropertyWithValue("name", "A");
        }

        @Test
        void doNotCreateCategoryWithNameTaken() throws Exception {
            // given
            categoryRepository.save(new Category("A"));
            // when
            var result = client.perform(post("/categories/create")
                    .param("name", "A")
                    .with(csrf()));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attributeExists("duplicatedName"),
                    view().name("category/category-form")
            );
            assertThat(categoryRepository.count()).isEqualTo(1);
        }

    }

}
