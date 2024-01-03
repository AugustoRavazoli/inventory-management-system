package io.github.augustoravazoli.inventorymanagementsystem.category;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Nested
    class CreateCategoryTests {

        @Test
        void createCategory() {
            // given
            when(categoryRepository.existsByName(anyString())).thenReturn(false);
            // when
            categoryService.createCategory(new Category("A"));
            // then
            verify(categoryRepository, times(1)).save(any(Category.class));
        }

        @Test
        void doNotCreateCategoryWithNameTaken() {
            // given
            when(categoryRepository.existsByName(anyString())).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> categoryService.createCategory(new Category("A")));
            // then
            exception.isInstanceOf(CategoryNameTakenException.class);
            verify(categoryRepository, never()).save(any(Category.class));
        }

    }

    @Nested
    class ListCategoriesTests {

        @Test
        void listCategoriesPaginated() {
            // given
            var expectedCategoryPage = new PageImpl<>(
                    List.of(new Category("A"), new Category("B"), new Category("C")),
                    PageRequest.of(0, 8, Sort.by("name")),
                    3
            );
            when(categoryRepository.findAll(PageRequest.of(0, 8, Sort.by("name"))))
                    .thenReturn(expectedCategoryPage);
            // when
            var actualCategoryPage = categoryService.listCategories(1);
            // then
            assertThat(actualCategoryPage.getContent()).extracting("name").isSorted();
            assertThat(actualCategoryPage).usingRecursiveComparison().isEqualTo(expectedCategoryPage);
        }

        @Test
        void listCategories() {
            // given
            var expectedCategories = List.of(new Category("A"), new Category("B"), new Category("C"));
            when(categoryRepository.findAll(Sort.by("name"))).thenReturn(expectedCategories);
            // when
            var actualCategories = categoryService.listCategories();
            // then
            assertThat(actualCategories).extracting("name").isSorted();
            assertThat(actualCategories).usingRecursiveComparison().isEqualTo(expectedCategories);
        }

    }

    @Nested
    class FindCategoryTests {

        @Test
        void findCategory() {
            // given
            var expectedCategory = new Category(1L, "A");
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(expectedCategory));
            // when
            var actualCategory = categoryService.findCategory(1L);
            // then
            assertThat(actualCategory).usingRecursiveComparison().isEqualTo(expectedCategory);
        }

        @Test
        void doNotFindCategoryThatDoesNotExists() {
            // given
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> categoryService.findCategory(1L));
            // then
            exception.isInstanceOf(CategoryNotFoundException.class);
        }

    }

    @Nested
    class UpdateCategoryTests {

        @Test
        void updateCategory() {
            // given
            var category = new Category(1L, "A");
            when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
            when(categoryRepository.existsByName(anyString())).thenReturn(false);
            // when
            categoryService.updateCategory(1L, new Category("B"));
            // then
            assertThat(category.getName()).isEqualTo("B");
            verify(categoryRepository, times(1)).save(category);
        }

        @Test
        void doNotUpdateCategoryThatDoesNotExists() {
            // given
            when(categoryRepository.findById(anyLong())).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> categoryService.updateCategory(1L, new Category("B")));
            // then
            exception.isInstanceOf(CategoryNotFoundException.class);
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        void doNotUpdateCategoryUsingNameTaken() {
            // given
            var category = new Category(1L, "A");
            when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
            when(categoryRepository.existsByName(anyString())).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> categoryService.updateCategory(1L, new Category("B")));
            // then
            exception.isInstanceOf(CategoryNameTakenException.class);
            verify(categoryRepository, never()).save(any(Category.class));
        }

    }

    @Nested
    class DeleteCategoryTests {

        @Test
        void deleteCategory() {
            // given
            when(categoryRepository.existsById(1L)).thenReturn(true);
            // when
            categoryService.deleteCategory(1L);
            // then
            verify(categoryRepository, times(1)).deleteById(1L);
        }

        @Test
        void doNotDeleteCategoryThatDoesNotExists() {
            // given
            when(categoryRepository.existsById(1L)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> categoryService.deleteCategory(1L));
            // then
            exception.isInstanceOf(CategoryNotFoundException.class);
            verify(categoryRepository, never()).deleteById(1L);
        }

    }

}