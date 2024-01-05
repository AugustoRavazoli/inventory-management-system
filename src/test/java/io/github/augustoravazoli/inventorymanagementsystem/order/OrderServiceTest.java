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

import static java.util.Collections.emptyList;
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

    private Customer customer;
    private Product productA;
    private Product productB;

    @BeforeEach
    void setup() {
        customer = new Customer(1L, "A", "A", "A");
        productA = new Product(1L, "A", new Category("A"), 10, new BigDecimal("1.00"));
        productB = new Product(2L, "B", new Category("B"), 20, new BigDecimal("2.00"));
    }

    @Nested
    class CreateOrderTests {

        @Test
        void createOrder() {
            // given
            var order = new OrderBuilder()
                    .status(Order.Status.UNPAID)
                    .customer(customer)
                    .item(5, productA)
                    .item(10, productB)
                    .build();
            when(customerRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of(productA, productB));
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
                    .customer(customer)
                    .build();
            when(customerRepository.existsById(1L)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> orderService.createOrder(order));
            // then
            exception.isInstanceOf(InvalidCustomerException.class);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotCreateOrderWithDuplicatedItems() {
            // given
            var order = new OrderBuilder()
                    .status(Order.Status.UNPAID)
                    .customer(customer)
                    .item(5, productA)
                    .item(10, productA)
                    .build();
            when(customerRepository.existsById(1L)).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> orderService.createOrder(order));
            // then
            exception.isInstanceOf(DuplicatedOrderItemException.class);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        void doNotCreateOrderWithNonexistentProducts() {
            // given
            var order = new OrderBuilder()
                    .status(Order.Status.UNPAID)
                    .customer(customer)
                    .item(5, productA)
                    .build();
            when(customerRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findAllById(List.of(1L))).thenReturn(emptyList());
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
                    .customer(customer)
                    .item(1000, productA)
                    .build();
            when(customerRepository.existsById(1L)).thenReturn(true);
            when(productRepository.findAllById(List.of(1L))).thenReturn(List.of(productA));
            // when
            var exception = assertThatThrownBy(() -> orderService.createOrder(order));
            // then
            exception.isInstanceOf(ProductWithInsufficientStockException.class);
            verify(orderRepository, never()).save(any(Order.class));
        }

    }

    @Nested
    class ListOrdersTests {

        private final List<Order> orders = List.of(
                new OrderBuilder()
                        .status(Order.Status.UNPAID)
                        .date(LocalDate.now())
                        .customer(customer)
                        .item(5, productA)
                        .item(10, productB)
                        .build(),
                new OrderBuilder()
                        .status(Order.Status.UNPAID)
                        .date(LocalDate.now())
                        .customer(customer)
                        .item(5, productA)
                        .item(10, productB)
                        .build(),
                new OrderBuilder()
                        .status(Order.Status.UNPAID)
                        .date(LocalDate.now())
                        .customer(customer)
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

}
