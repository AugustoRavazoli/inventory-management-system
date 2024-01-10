package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
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
                    .status(Order.Status.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(10, productB)
                    .build();
            when(customerRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findById(1L)).thenReturn(Optional.of(productA));
            when(productRepository.findById(2L)).thenReturn(Optional.of(productB));
            // when
            orderService.createOrder(order);
            // then
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(10);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        void doNotCreateOrderWithNonexistentCustomer() {
            // given
            var order = new OrderBuilder()
                    .status(Order.Status.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(10, productB)
                    .build();
            when(customerRepository.existsById(1L)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> orderService.createOrder(order));
            // then
            exception.isInstanceOf(InvalidCustomerException.class);
            assertThat(productA.getQuantity()).isEqualTo(10);
            assertThat(productB.getQuantity()).isEqualTo(20);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotCreateOrderWithDuplicatedItems() {
            // given
            var order = new OrderBuilder()
                    .status(Order.Status.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(10, productA)
                    .build();
            when(customerRepository.existsById(1L)).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> orderService.createOrder(order));
            // then
            exception.isInstanceOf(DuplicatedOrderItemException.class);
            assertThat(productA.getQuantity()).isEqualTo(10);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotCreateOrderWithNonexistentProducts() {
            // given
            var order = new OrderBuilder()
                    .status(Order.Status.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .build();
            when(customerRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findById(1L)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> orderService.createOrder(order));
            // then
            exception.isInstanceOf(InvalidProductException.class);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotCreateOrderWithProductsWithInsufficientStock() {
            // given
            var order = new OrderBuilder()
                    .status(Order.Status.UNPAID)
                    .customer(customerA)
                    .item(1000, productA)
                    .build();
            when(customerRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findById(1L)).thenReturn(Optional.of(productA));
            // when
            var exception = assertThatThrownBy(() -> orderService.createOrder(order));
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
                        .status(Order.Status.UNPAID)
                        .date(LocalDate.now())
                        .customer(customerA)
                        .item(5, productA)
                        .item(10, productB)
                        .build(),
                new OrderBuilder()
                        .status(Order.Status.UNPAID)
                        .date(LocalDate.now())
                        .customer(customerA)
                        .item(5, productA)
                        .item(10, productB)
                        .build(),
                new OrderBuilder()
                        .status(Order.Status.UNPAID)
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
            when(orderRepository.findAllByStatus(Order.Status.UNPAID, pageable)).thenReturn(expectedOrderPage);
            // when
            var actualOrderPage = orderService.listOrders(Order.Status.UNPAID, 1);
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
                            .status(Order.Status.UNPAID)
                            .date(LocalDate.now())
                            .customer(customerA)
                            .item(5, productA)
                            .item(10, productB)
                            .build(),
                    new OrderBuilder()
                            .status(Order.Status.UNPAID)
                            .date(LocalDate.now())
                            .customer(customerA)
                            .item(5, productA)
                            .item(10, productB)
                            .build()
            );
            when(orderRepository.findAllByStatusAndCustomerNameContainingIgnoreCase(Order.Status.UNPAID, "A"))
                    .thenReturn(expectedOrders);
            // when
            var actualOrders = orderService.findOrders(Order.Status.UNPAID, "A");
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
                    .status(Order.Status.UNPAID)
                    .date(LocalDate.now())
                    .customer(customerA)
                    .item(5, productA)
                    .item(10, productB)
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(expectedOrder));
            // when
            var actualOrder = orderService.findOrder(1L);
            // then
            assertThat(actualOrder).usingRecursiveComparison().isEqualTo(expectedOrder);
        }

        @Test
        void doNotFindOrderThatDoesNotExists() {
            // given
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> orderService.findOrder(1L));
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
                    .status(Order.Status.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(8, productB)
                    .build();
        }

        @Test
        void updateOrderWithNewItemsDecreaseProductStock() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(Order.Status.PAID)
                    .customer(customerB)
                    .item(5, productA)
                    .item(8, productB)
                    .item(15, productC)
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(customerRepository.existsById(2L)).thenReturn(true);
            when(productRepository.findById(1L)).thenReturn(Optional.of(productA));
            when(productRepository.findById(2L)).thenReturn(Optional.of(productB));
            when(productRepository.findById(3L)).thenReturn(Optional.of(productC));
            // when
            orderService.updateOrder(1L, updatedOrder);
            // then
            assertThat(order).usingRecursiveComparison().isEqualTo(updatedOrder);
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(12);
            assertThat(productC.getQuantity()).isEqualTo(15);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        void updateOrderWithDeletedItemsResetProductStock() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(Order.Status.PAID)
                    .customer(customerB)
                    .item(5, productA)
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(customerRepository.existsById(2L)).thenReturn(true);
            when(productRepository.findById(1L)).thenReturn(Optional.of(productA));
            when(productRepository.findById(2L)).thenReturn(Optional.of(productB));
            // when
            orderService.updateOrder(1L, updatedOrder);
            // then
            assertThat(order).usingRecursiveComparison().isEqualTo(updatedOrder);
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(20);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        void updateOrderWithSameItemsButDifferentQuantitiesChangeStock() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(Order.Status.PAID)
                    .customer(customerB)
                    .item(3, productA)
                    .item(10, productB)
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(customerRepository.existsById(2L)).thenReturn(true);
            when(productRepository.findById(1L)).thenReturn(Optional.of(productA));
            when(productRepository.findById(2L)).thenReturn(Optional.of(productB));
            // when
            orderService.updateOrder(1L, updatedOrder);
            // then
            assertThat(order).usingRecursiveComparison().isEqualTo(updatedOrder);
            assertThat(productA.getQuantity()).isEqualTo(7);
            assertThat(productB.getQuantity()).isEqualTo(10);
            verify(orderRepository, times(1)).save(order);
        }

        @Test
        void doNotUpdateOrderThatDoesNotExists() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(Order.Status.PAID)
                    .customer(customerB)
                    .item(3, productA)
                    .item(10, productB)
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> orderService.updateOrder(1L, updatedOrder));
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
                    .status(Order.Status.PAID)
                    .customer(customerB)
                    .item(3, productA)
                    .item(10, productB)
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(customerRepository.existsById(2L)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> orderService.updateOrder(1L, updatedOrder));
            // then
            exception.isInstanceOf(InvalidCustomerException.class);
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(12);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotUpdateOrderUsingDuplicatedItems() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(Order.Status.PAID)
                    .customer(customerB)
                    .item(3, productA)
                    .item(3, productA)
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(customerRepository.existsById(2L)).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> orderService.updateOrder(1L, updatedOrder));
            // then
            exception.isInstanceOf(DuplicatedOrderItemException.class);
            assertThat(productA.getQuantity()).isEqualTo(5);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotUpdateOrderUsingNonexistentProducts() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(Order.Status.PAID)
                    .customer(customerB)
                    .item(3, productA)
                    .item(10, productB)
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(customerRepository.existsById(2L)).thenReturn(true);
            when(productRepository.findById(1L)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> orderService.updateOrder(1L, updatedOrder));
            // then
            exception.isInstanceOf(InvalidProductException.class);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotUpdateOrderUsingProductsWithInsufficientStock() {
            // given
            var updatedOrder = new OrderBuilder()
                    .status(Order.Status.PAID)
                    .customer(customerB)
                    .item(100, productA)
                    .item(100, productB)
                    .build();
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(customerRepository.existsById(2L)).thenReturn(true);
            when(productRepository.findById(1L)).thenReturn(Optional.of(productA));
            var exception = assertThatThrownBy(() -> orderService.updateOrder(1L, updatedOrder));
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
                    .status(Order.Status.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(8, productB)
                    .build();
        }

        @Test
        void deleteUnpaidOrderResetProductStock() {
            // given
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            when(productRepository.findById(1L)).thenReturn(Optional.of(productA));
            when(productRepository.findById(2L)).thenReturn(Optional.of(productB));
            // when
            orderService.deleteOrder(1L);
            // then
            assertThat(productA.getQuantity()).isEqualTo(10);
            assertThat(productB.getQuantity()).isEqualTo(20);
            verify(orderRepository, times(1)).delete(order);
        }

        @Test
        void deletePaidOrderDoesNotChangeProductStock() {
            // given
            order.setStatus(Order.Status.PAID);
            when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
            // when
            orderService.deleteOrder(1L);
            // then
            assertThat(productA.getQuantity()).isEqualTo(5);
            assertThat(productB.getQuantity()).isEqualTo(12);
            verify(productRepository, never()).deleteById(anyLong());
            verify(orderRepository, times(1)).delete(order);
        }

        @Test
        void doNotDeleteOrderThatDoesNotExists() {
            // given
            when(orderRepository.findById(1L)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> orderService.deleteOrder(1L));
            // then
            exception.isInstanceOf(OrderNotFoundException.class);
            verify(productRepository, never()).findById(anyLong());
            verify(orderRepository, never()).delete(any(Order.class));
        }

    }

}
