package io.github.augustoravazoli.inventorymanagementsystem.category;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
@WithMockUser
class CategoryControllerTest {

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private MockMvc client;

    @Nested
    class CreateCategoryTests {

        @Test
        void retrieveCreateCategoryPage() throws Exception {
            // when
            var result = client.perform(get("/categories/create"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("category", hasProperty("name")),
                    model().attribute("mode", "create"),
                    view().name("category/category-form")
            );
        }

        @Test
        void createCategory() throws Exception {
            // given
            doNothing().when(categoryService).createCategory(any(Category.class));
            // when
            var result = client.perform(post("/categories/create")
                    .param("name", "A")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/categories/list")
            );
            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        void doNotCreateCategoryWithNameTaken() throws Exception {
            // given
            doThrow(CategoryNameTakenException.class).when(categoryService).createCategory(any(Category.class));
            // when
            var result = client.perform(post("/categories/create")
                    .param("name", "A")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("duplicatedName", is(true)),
                    model().attribute("category", hasProperty("name", is("A"))),
                    model().attribute("mode", "create"),
                    view().name("category/category-form")
            );
            verify(categoryService).createCategory(any(Category.class));
        }

        @Test
        void doNotCreateCategoryWithBlankName() throws Exception {
            // when
            var result = client.perform(post("/categories/create")
                    .param("name", "")
                    .with(csrf())
            );
            // then
            result.andExpect(status().isBadRequest());
        }

    }

}