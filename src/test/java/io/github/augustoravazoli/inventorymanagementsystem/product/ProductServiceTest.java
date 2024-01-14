package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import io.github.augustoravazoli.inventorymanagementsystem.order.OrderRepository;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

    @Mock
    private OrderRepository orderRepository;

    private final User user = new User();

    @Nested
    class CreateProductTests {

        @Test
        void createProduct() {
            // given
            var product = new Product("A", new Category(1L, "A"), 1, "1.00");
            when(productRepository.existsByNameAndOwner("A", user)).thenReturn(false);
            when(categoryRepository.existsByIdAndOwner(1L, user)).thenReturn(true);
            // when
            productService.createProduct(product, user);
            // then
            assertThat(product.getOwner()).isEqualTo(user);
            verify(productRepository, times(1)).save(product);
        }

        @Test
        void doNotCreateProductWithNameTaken() {
            // given
            var product = new Product("A", new Category(1L, "A"), 1, "1.00");
            when(productRepository.existsByNameAndOwner("A", user)).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> productService.createProduct(product, user));
            // then
            exception.isInstanceOf(ProductNameTakenException.class);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        void doNotCreateProductWithNonexistentCategory() {
            // given
            var product = new Product("A", new Category(1L, "A"), 1, "1.00");
            when(productRepository.existsByNameAndOwner("A", user)).thenReturn(false);
            when(categoryRepository.existsByIdAndOwner(1L, user)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> productService.createProduct(product, user));
            // then
            exception.isInstanceOf(InvalidCategoryException.class);
            verify(productRepository, never()).save(any(Product.class));
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
            when(productRepository.findAllByOwner(user, pageable)).thenReturn(expectedProductPage);
            // when
            var actualProductPage = productService.listProducts(1, user);
            // then
            assertThat(actualProductPage.getContent()).extracting("name").isSorted();
            assertThat(actualProductPage).usingRecursiveComparison().isEqualTo(expectedProductPage);
        }

        @Test
        void listProducts() {
            // given
            var expectedProducts = products;
            when(productRepository.findAllByOwner(user, Sort.by("name"))).thenReturn(expectedProducts);
            // when
            var actualProducts = productService.listProducts(user);
            // then
            assertThat(actualProducts).extracting("name").isSorted();
            assertThat(actualProducts).usingRecursiveComparison().isEqualTo(expectedProducts);
        }

    }

    @Nested
    class FindProductsTests {

        @Test
        void findProducts() {
            // given
            var expectedProducts = List.of(
                    new Product("A", new Category("A"), 1, "1.00"),
                    new Product("Aa", new Category("Aa"), 2, "2.00")
            );
            when(productRepository.findAllByNameContainingIgnoreCaseAndOwner("A", user)).thenReturn(expectedProducts);
            // when
            var actualProducts = productService.findProducts("A", user);
            // then
            assertThat(actualProducts).extracting("name").isSorted();
            assertThat(actualProducts).usingRecursiveComparison().isEqualTo(expectedProducts);
        }

    }

    @Nested
    class FindProductTests {

        @Test
        void findProduct() {
            // given
            var expectedProduct = new Product(1L, "A", new Category("A"), 1, new BigDecimal("1.00"));
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(expectedProduct));
            // when
            var actualProduct = productService.findProduct(1L, user);
            // then
            assertThat(actualProduct).usingRecursiveComparison().isEqualTo(expectedProduct);
        }

        @Test
        void doNotFindProductThatDoesNotExists() {
            // given
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> productService.findProduct(1L, user));
            // then
            exception.isInstanceOf(ProductNotFoundException.class);
        }

    }

    @Nested
    class UpdateProductTests {

        @Test
        void updateProduct() {
            // given
            var product = new Product("A", new Category("A"), 1,"1.00");
            product.setOwner(user);
            var updatedProduct = new Product("B", new Category(2L, "B"), 2, "2.00");
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(product));
            when(productRepository.existsByNameAndOwner("B", user)).thenReturn(false);
            when(categoryRepository.existsByIdAndOwner(2L, user)).thenReturn(true);
            // when
            productService.updateProduct(1L, updatedProduct, user);
            // then
            assertThat(product).usingRecursiveComparison().ignoringFields("owner").isEqualTo(updatedProduct);
            assertThat(product.getOwner()).isEqualTo(user);
            verify(productRepository, times(1)).save(product);
        }

        @Test
        void doNotUpdateProductThatDoesNotExists() {
            // given
            var updatedProduct = new Product("B", new Category(2L, "B"), 2, "2.00");
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> productService.updateProduct(1L, updatedProduct, user));
            // then
            exception.isInstanceOf(ProductNotFoundException.class);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        void doNotUpdateProductUsingNameTaken() {
            // given
            var product = new Product("A", new Category("A"), 1,"1.00");
            var updatedProduct = new Product("B", new Category(2L, "B"), 2, "2.00");
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(product));
            when(productRepository.existsByNameAndOwner("B", user)).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> productService.updateProduct(1L, updatedProduct, user));
            // then
            exception.isInstanceOf(ProductNameTakenException.class);
            verify(productRepository, never()).save(any(Product.class));
        }

        @Test
        void doNotUpdateProductUsingNonexistentCategory() {
            // given
            var product = new Product("A", new Category("A"), 1,"1.00");
            var updatedProduct = new Product("B", new Category(2L, "B"), 2, "2.00");
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(product));
            when(productRepository.existsByNameAndOwner("B", user)).thenReturn(false);
            when(categoryRepository.existsByIdAndOwner(2L, user)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> productService.updateProduct(1L, updatedProduct, user));
            // then
            exception.isInstanceOf(InvalidCategoryException.class);
            verify(productRepository, never()).save(any(Product.class));
        }

    }

    @Nested
    class DeleteProductTests {

        @Test
        void deleteProduct() {
            // given
            when(productRepository.existsByIdAndOwner(1L, user)).thenReturn(true);
            when(orderRepository.existsByItemsProductIdAndOwner(1L, user)).thenReturn(false);
            // when
            productService.deleteProduct(1L, user);
            // then
            verify(productRepository, times(1)).deleteById(1L);
        }

        @Test
        void doNotDeleteProductThatDoesNotExists() {
            // given
            when(productRepository.existsByIdAndOwner(1L, user)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> productService.deleteProduct(1L, user));
            // then
            exception.isInstanceOf(ProductNotFoundException.class);
            verify(productRepository, never()).deleteById(anyLong());
        }

        @Test
        void doNotDeleteProductAssociatedWithOrders() {
            // given
            when(productRepository.existsByIdAndOwner(1L, user)).thenReturn(true);
            when(orderRepository.existsByItemsProductIdAndOwner(1L, user)).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> productService.deleteProduct(1L, user));
            // then
            exception.isInstanceOf(ProductDeletionNotAllowedException.class);
            verify(productRepository, never()).deleteById(anyLong());
        }

    }

}
