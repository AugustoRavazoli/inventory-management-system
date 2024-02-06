package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CustomerRepository customerRepository;

    private Customer customerA;
    private Customer customerB;
    private Product productA;
    private Product productB;
    private Product productC;
    private final User user = new User();

    @BeforeEach
    void setup() {
        customerA = new Customer(1L, "A", "A", "A");
        customerB = new Customer(2L, "B", "B", "B");
        productA = new Product(1L, "A", new Category("A"), 10, new BigDecimal("1.00"));
        productB = new Product(2L, "B", new Category("B"), 20, new BigDecimal("2.00"));
        productC = new Product(3L, "C", new Category("C"), 30, new BigDecimal("3.00"));
    }

    @Nested
    class CreateOrderTests {

        @Test
        void createOrder() {
            // given
            var order = new OrderBuilder()
                    .status(OrderStatus.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(10, productB)
                    .build();
            when(customerRepository.existsByIdAndOwner(1L, user)).thenReturn(true);
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(productA));
            when(productRepository.findByIdAndOwner(2L, user)).thenReturn(Optional.of(productB));
            // when
            orderService.createOrder(order, user);
            // then
            assertThat(order.getOwner()).isEqualTo(user);
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(10);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        void doNotCreateOrderWithNonexistentCustomer() {
            // given
            var order = new OrderBuilder()
                    .status(OrderStatus.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(10, productB)
                    .build();
            when(customerRepository.existsByIdAndOwner(1L, user)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> orderService.createOrder(order, user));
            // then
            exception.isInstanceOf(InvalidCustomerException.class);
            assertThat(productA.getQuantity()).isEqualTo(10);
            assertThat(productB.getQuantity()).isEqualTo(20);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotCreateOrderWithNonexistentProducts() {
            // given
            var order = new OrderBuilder()
                    .status(OrderStatus.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .build();
            when(customerRepository.existsByIdAndOwner(1L, user)).thenReturn(true);
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> orderService.createOrder(order, user));
            // then
            exception.isInstanceOf(InvalidProductException.class);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotCreateOrderWithProductsWithInsufficientStock() {
            // given
            var order = new OrderBuilder()
                    .status(OrderStatus.UNPAID)
                    .customer(customerA)
                    .item(1000, productA)
                    .build();
            when(customerRepository.existsByIdAndOwner(1L, user)).thenReturn(true);
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(productA));
            // when
            var exception = assertThatThrownBy(() -> orderService.createOrder(order, user));
            // then
            exception.isInstanceOf(ProductWithInsufficientStockException.class);
            assertThat(productA.getQuantity()).isEqualTo(10);
            verify(orderRepository, never()).save(any(Order.class));
        }

    }

    @Nested
    class ListOrdersTests {

        private final List<Order> orders = List.of(
                new OrderBuilder()
                        .status(OrderStatus.UNPAID)
                        .date(LocalDate.now())
                        .customer(customerA)
                        .item(5, productA)
                        .item(10, productB)
                        .build(),
                new OrderBuilder()
                        .status(OrderStatus.UNPAID)
                        .date(LocalDate.now())
                        .customer(customerA)
                        .item(5, productA)
                        .item(10, productB)
                        .build(),
                new OrderBuilder()
                        .status(OrderStatus.UNPAID)
                        .date(LocalDate.now())
                        .customer(customerA)
                        .item(5, productA)
                        .item(10, productB)
                        .build()
        );

        @Test
        void listOrdersPaginated() {
            // given
            var pageable = PageRequest.of(0, 8, Sort.by("date"));
            var expectedOrderPage = new PageImpl<>(orders, pageable, 3);
            when(orderRepository.findAllByStatusAndOwner(OrderStatus.UNPAID, user, pageable)).thenReturn(expectedOrderPage);
            // when
            var actualOrderPage = orderService.listOrders(OrderStatus.UNPAID, 1, user);
            // then
            assertThat(actualOrderPage.getContent()).extracting("date").isSorted();
            assertThat(actualOrderPage).usingRecursiveComparison().isEqualTo(expectedOrderPage);
        }

    }

    @Nested
    class FindOrdersTests {

        @Test
        void findOrders() {
            // given
            var expectedOrders = List.of(
                    new OrderBuilder()
                            .status(OrderStatus.UNPAID)
                            .date(LocalDate.now())
                            .customer(customerA)
                            .item(5, productA)
                            .item(10, productB)
                            .build(),
                    new OrderBuilder()
                            .status(OrderStatus.UNPAID)
                            .date(LocalDate.now())
                            .customer(customerA)
                            .item(5, productA)
                            .item(10, productB)
                            .build()
            );
            when(orderRepository.findAllByStatusAndCustomerNameContainingIgnoreCaseAndOwner(OrderStatus.UNPAID, "A", user))
                    .thenReturn(expectedOrders);
            // when
            var actualOrders = orderService.findOrders(OrderStatus.UNPAID, "A", user);
            // then
            assertThat(actualOrders).extracting("customer.name").isSorted();
            assertThat(actualOrders).usingRecursiveComparison().isEqualTo(expectedOrders);
        }

    }

    @Nested
    class FindOrderTests {

        @Test
        void findOrder() {
            // given
            var expectedOrder =  new OrderBuilder()
                    .status(OrderStatus.UNPAID)
                    .date(LocalDate.now())
                    .customer(customerA)
                    .item(5, productA)
                    .item(10, productB)
                    .build();
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(expectedOrder));
            // when
            var actualOrder = orderService.findOrder(1L, user);
            // then
            assertThat(actualOrder).usingRecursiveComparison().isEqualTo(expectedOrder);
        }

        @Test
        void doNotFindOrderThatDoesNotExists() {
            // given
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> orderService.findOrder(1L, user));
            // then
            exception.isInstanceOf(OrderNotFoundException.class);
        }

    }

    @Nested
    class UpdateOrderTests {

        private Order order;

        @BeforeEach
        void setup() {
            productA.setQuantity(5);
            productB.setQuantity(12);
            order = new OrderBuilder()
                    .id(1L)
                    .status(OrderStatus.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(8, productB)
                    .owner(user)
                    .build();
        }

        @Test
        void updateOrderWithNewItemsDecreaseProductStock() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(OrderStatus.PAID)
                    .customer(customerB)
                    .item(5, productA)
                    .item(8, productB)
                    .item(15, productC)
                    .build();
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(order));
            when(customerRepository.existsByIdAndOwner(2L, user)).thenReturn(true);
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(productA));
            when(productRepository.findByIdAndOwner(2L, user)).thenReturn(Optional.of(productB));
            when(productRepository.findByIdAndOwner(3L, user)).thenReturn(Optional.of(productC));
            // when
            orderService.updateOrder(1L, updatedOrder, user);
            // then
            assertThat(order.getOwner()).isEqualTo(user);
            assertThat(order).usingRecursiveComparison().ignoringFields("id", "owner").isEqualTo(updatedOrder);
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(12);
            assertThat(productC.getQuantity()).isEqualTo(15);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        void updateOrderWithDeletedItemsResetProductStock() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(OrderStatus.PAID)
                    .customer(customerB)
                    .item(5, productA)
                    .build();
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(order));
            when(customerRepository.existsByIdAndOwner(2L, user)).thenReturn(true);
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(productA));
            when(productRepository.findByIdAndOwner(2L, user)).thenReturn(Optional.of(productB));
            // when
            orderService.updateOrder(1L, updatedOrder, user);
            // then
            assertThat(order.getOwner()).isEqualTo(user);
            assertThat(order).usingRecursiveComparison().ignoringFields("id", "owner").isEqualTo(updatedOrder);
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(20);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        void updateOrderWithSameItemsButDifferentQuantitiesChangeStock() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(OrderStatus.PAID)
                    .customer(customerB)
                    .item(3, productA)
                    .item(14, productB)
                    .build();
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(order));
            when(customerRepository.existsByIdAndOwner(2L, user)).thenReturn(true);
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(productA));
            when(productRepository.findByIdAndOwner(2L, user)).thenReturn(Optional.of(productB));
            // when
            orderService.updateOrder(1L, updatedOrder, user);
            // then
            assertThat(order.getOwner()).isEqualTo(user);
            assertThat(order).usingRecursiveComparison().ignoringFields("id", "owner").isEqualTo(updatedOrder);
            assertThat(productA.getQuantity()).isEqualTo(7);
            assertThat(productB.getQuantity()).isEqualTo(6);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        void updateOrderWithSameItemsAndQuantitiesButProductsWithEmptyStockDoesNotThrowException() {
            // given
            productA.setQuantity(0);
            productB.setQuantity(0);
            var updatedOrder = new OrderBuilder()
                    .status(OrderStatus.PAID)
                    .customer(customerB)
                    .item(5, productA)
                    .item(8, productB)
                    .build();
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(order));
            when(customerRepository.existsByIdAndOwner(2L, user)).thenReturn(true);
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(productA));
            when(productRepository.findByIdAndOwner(2L, user)).thenReturn(Optional.of(productB));
            // when
            orderService.updateOrder(1L, updatedOrder, user);
            // then
            assertThat(order.getOwner()).isEqualTo(user);
            assertThat(order).usingRecursiveComparison().ignoringFields("id", "owner").isEqualTo(updatedOrder);
            assertThat(productA.getQuantity()).isEqualTo(0);
            assertThat(productB.getQuantity()).isEqualTo(0);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        void doNotUpdateOrderThatDoesNotExists() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(OrderStatus.PAID)
                    .customer(customerB)
                    .item(3, productA)
                    .item(10, productB)
                    .build();
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> orderService.updateOrder(1L, updatedOrder, user));
            // then
            exception.isInstanceOf(OrderNotFoundException.class);
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(12);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotUpdateOrderUsingNonexistentCustomer() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(OrderStatus.PAID)
                    .customer(customerB)
                    .item(3, productA)
                    .item(10, productB)
                    .build();
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(order));
            when(customerRepository.existsByIdAndOwner(2L, user)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> orderService.updateOrder(1L, updatedOrder, user));
            // then
            exception.isInstanceOf(InvalidCustomerException.class);
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(12);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotUpdateOrderUsingNonexistentProducts() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(OrderStatus.PAID)
                    .customer(customerB)
                    .item(3, productA)
                    .item(10, productB)
                    .build();
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(order));
            when(customerRepository.existsByIdAndOwner(2L, user)).thenReturn(true);
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> orderService.updateOrder(1L, updatedOrder, user));
            // then
            exception.isInstanceOf(InvalidProductException.class);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotUpdateOrderUsingProductsWithInsufficientStock() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(OrderStatus.PAID)
                    .customer(customerB)
                    .item(100, productA)
                    .item(100, productB)
                    .build();
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(order));
            when(customerRepository.existsByIdAndOwner(2L, user)).thenReturn(true);
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(productA));
            var exception = assertThatThrownBy(() -> orderService.updateOrder(1L, updatedOrder, user));
            // then
            exception.isInstanceOf(ProductWithInsufficientStockException.class);
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(12);
            verify(orderRepository, never()).save(any(Order.class));
        }

    }

    @Nested
    class DeleteOrderTests {

        private Order order;

        @BeforeEach
        void setup() {
            productA.setQuantity(5);
            productB.setQuantity(12);
            order = new OrderBuilder()
                    .status(OrderStatus.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(8, productB)
                    .build();
        }

        @Test
        void deleteUnpaidOrderResetProductStock() {
            // given
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(order));
            when(productRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(productA));
            when(productRepository.findByIdAndOwner(2L, user)).thenReturn(Optional.of(productB));
            // when
            orderService.deleteOrder(1L, user);
            // then
            assertThat(productA.getQuantity()).isEqualTo(10);
            assertThat(productB.getQuantity()).isEqualTo(20);
            verify(orderRepository, times(1)).delete(order);
        }

        @Test
        void deletePaidOrderDoesNotChangeProductStock() {
            // given
            order.setStatus(OrderStatus.PAID);
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(order));
            // when
            orderService.deleteOrder(1L, user);
            // then
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(12);
            verify(productRepository, never()).deleteById(anyLong());
            verify(orderRepository, times(1)).delete(order);
        }

        @Test
        void doNotDeleteOrderThatDoesNotExists() {
            // given
            when(orderRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> orderService.deleteOrder(1L, user));
            // then
            exception.isInstanceOf(OrderNotFoundException.class);
            verify(productRepository, never()).findById(anyLong());
            verify(orderRepository, never()).delete(any(Order.class));
        }

    }

}
