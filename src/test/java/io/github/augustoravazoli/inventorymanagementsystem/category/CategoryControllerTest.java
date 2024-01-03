package io.github.augustoravazoli.inventorymanagementsystem.category;

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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
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

    @Nested
    class ListCategoriesTests {

        @Test
        void listCategories() throws Exception {
            // given
            when(categoryService.listCategories(1)).thenReturn(new PageImpl<>(
                    List.of(new Category("A"), new Category("B"), new Category("C")),
                    PageRequest.of(0, 8, Sort.by("name")),
                    3
            ));
            // when
            var result = client.perform(get("/categories/list"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("categories", contains(
                            allOf(hasProperty("name", is("A"))),
                            allOf(hasProperty("name", is("B"))),
                            allOf(hasProperty("name", is("C")))
                    )),
                    model().attribute("currentPage", 1),
                    model().attribute("totalPages", 1),
                    view().name("category/category-table")
            );
            verify(categoryService, times(1)).listCategories(anyInt());
        }

    }

    @Nested
    class FindCategoriesTests {

        @Test
        void findCategories() throws Exception {
            // given
            var categories = List.of(new Category("A"), new Category("Aa"));
            when(categoryService.findCategories("A")).thenReturn(categories);
            // when
            var result = client.perform(get("/categories/find")
                    .param("name", "A")
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("categories", contains(
                            allOf(hasProperty("name", is("A"))),
                            allOf(hasProperty("name", is("Aa")))
                    ))
            );
            verify(categoryService, times(1)).findCategories(anyString());
        }

    }

    @Nested
    class UpdateCategoryTests {

        @Test
        void retrieveUpdateCategoryPage() throws Exception {
            // given
            when(categoryService.findCategory(1L)).thenReturn(new Category(1L, "A"));
            // when
            var result = client.perform(get("/categories/update/{id}", 1));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("category", hasProperty("name", is("A"))),
                    model().attribute("id", 1L),
                    model().attribute("mode", "update"),
                    view().name("category/category-form")
            );
        }

        @Test
        void updateCategory() throws Exception {
            // when
            var result = client.perform(post("/categories/update/{id}", 1L)
                    .param("name", "A")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/categories/list")
            );
            verify(categoryService, times(1)).updateCategory(anyLong(), any(Category.class));
        }

        @Test
        void doNotUpdateCategoryUsingNameTaken() throws Exception {
            // given
            doThrow(CategoryNameTakenException.class).when(categoryService).updateCategory(anyLong(), any(Category.class));
            // when
            var result = client.perform(post("/categories/update/{id}", 1L)
                    .param("name", "A")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("duplicatedName", true),
                    model().attribute("category", hasProperty("name", is("A"))),
                    model().attribute("id", 1L),
                    model().attribute("mode", "update"),
                    view().name("category/category-form")
            );
            verify(categoryService, times(1)).updateCategory(anyLong(), any(Category.class));
        }

        @Test
        void doNotUpdateCategoryUsingBlankName() throws Exception {
            // when
            var result = client.perform(post("/categories/update/{id}", 1L)
                    .param("name", "")
                    .with(csrf())
            );
            // then
            result.andExpect(status().isBadRequest());
        }

    }

    @Nested
    class DeleteCategoryTests {

        @Test
        void deleteCategory() throws Exception {
            // when
            var result = client.perform(post("/categories/delete/{id}", 1L)
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/categories/list")
            );
            verify(categoryService, times(1)).deleteCategory(anyLong());
        }

    }

}