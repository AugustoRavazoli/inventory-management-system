package io.github.augustoravazoli.inventorymanagementsystem.category;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}