package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Nested
    class CreateProductTests {

        @Test
        void createProduct() {
            // given
            var product = new Product("A", new Category(1L, "A"), 1, "1.00");
            when(productRepository.existsByName("A")).thenReturn(false);
            when(categoryRepository.existsById(1L)).thenReturn(true);
            // when
            productService.createProduct(product);
            // then
            verify(productRepository, times(1)).save(product);
        }

        @Test
        void doNotCreateProductWithNameTaken() {
            // given
            var product = new Product("A", new Category(1L, "A"), 1, "1.00");
            when(productRepository.existsByName("A")).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> productService.createProduct(product));
            // then
            exception.isInstanceOf(ProductNameTakenException.class);
            verify(productRepository, never()).save(product);
        }

        @Test
        void doNotCreateProductWithNonexistentCategory() {
            // given
            var product = new Product("A", new Category(1L, "A"), 1, "1.00");
            when(productRepository.existsByName("A")).thenReturn(false);
            when(categoryRepository.existsById(1L)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> productService.createProduct(product));
            // then
            exception.isInstanceOf(InvalidCategoryException.class);
            verify(productRepository, never()).save(product);
        }

    }

    @Nested
    class ListProductsTests {

        private final List<Product> products = List.of(
                new Product("A", new Category("A"), 1, "1.00"),
                new Product("B", new Category("B"), 2, "2.00"),
                new Product("C", new Category("C"), 3, "3.00")
        );

        @Test
        void listProductsPaginated() {
            // given
            var pageable = PageRequest.of(0, 8, Sort.by("name"));
            var expectedProductPage = new PageImpl<>(products, pageable, 3);
            when(productRepository.findAll(pageable)).thenReturn(expectedProductPage);
            // when
            var actualProductPage = productService.listProducts(1);
            // then
            assertThat(actualProductPage.getContent()).extracting("name").isSorted();
            assertThat(actualProductPage).usingRecursiveComparison().isEqualTo(expectedProductPage);
        }

        @Test
        void listProducts() {
            // given
            var expectedProducts = products;
            when(productRepository.findAll(Sort.by("name"))).thenReturn(expectedProducts);
            // when
            var actualProducts = productService.listProducts();
            // then
            assertThat(actualProducts).extracting("name").isSorted();
            assertThat(actualProducts).usingRecursiveComparison().isEqualTo(expectedProducts);
        }

    }

}