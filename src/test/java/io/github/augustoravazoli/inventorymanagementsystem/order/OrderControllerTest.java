package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.MockUserDetailsService;
import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerService;
import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductService;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerMatchers.customer;
import static io.github.augustoravazoli.inventorymanagementsystem.order.OrderMatchers.item;
import static io.github.augustoravazoli.inventorymanagementsystem.order.OrderMatchers.order;
import static io.github.augustoravazoli.inventorymanagementsystem.product.ProductMatchers.product;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(MockUserDetailsService.class)
@WithUserDetails
class OrderControllerTest {

    @MockBean
    private OrderService orderService;

    @MockBean
    private ProductService productService;

    @MockBean
    private CustomerService customerService;

    @Autowired
    private MockMvc client;

    private Customer customerA = new Customer(1L, "A", "A", "A");
    private Customer customerB = new Customer(2L, "B", "B", "B");
    private Product productA = new Product(1L, "A", new Category("A"), 10, new BigDecimal("1.00"));
    private Product productB = new Product(2L, "B", new Category("B"), 20, new BigDecimal("2.00"));

    @Nested
    class CreateOrderTests {

        @Test
        void retrieveCreateOrderPage() throws Exception {
            // given
            when(customerService.listCustomers()).thenReturn(List.of(customerA, customerB));
            when(productService.listProducts(any(User.class))).thenReturn(List.of(productA, productB));
            // when
            var result = client.perform(get("/orders/create"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("order", is(order())),
                    model().attribute("customers", contains(
                            customer(1L, "A", "A", "A"),
                            customer(2L, "B", "B", "B")
                    )),
                    model().attribute("products", contains(
                            product(1L, "A", "A", 10, "1.00"),
                            product(2L, "B", "B", 20, "2.00")
                    )),
                    model().attribute("mode", "create"),
                    view().name("order/order-form")
            );
        }

        @Test
        void createOrder() throws Exception {
            // when
            var result = client.perform(post("/orders/create")
                    .param("status", "UNPAID")
                    .param("customerId", "1")
                    .param("items[0].quantity", "5")
                    .param("items[0].productId", "1")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/orders/list?status=UNPAID")
            );
            verify(orderService, times(1)).createOrder(any(Order.class));
        }

        @ParameterizedTest
        @ArgumentsSource(AttributeNameAndExceptionProvider.class)
        void doNotCreateOrderWithInvalidItems(String attributeName, Class<? extends RuntimeException> exception) throws Exception {
            // given
            when(customerService.listCustomers()).thenReturn(List.of(customerA, customerB));
            when(productService.listProducts(any(User.class))).thenReturn(List.of(productA, productB));
            doThrow(exception).when(orderService).createOrder(any(Order.class));
            // when
            var result = client.perform(post("/orders/create")
                    .param("status", "UNPAID")
                    .param("customerId", "1")
                    .param("items[0].quantity", "5")
                    .param("items[0].productId", "1")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute(attributeName, true),
                    model().attribute("order", is(
                            order("UNPAID", 1L, contains(item(5, 1L)))
                    )),
                    model().attribute("customers", contains(
                            customer(1L, "A", "A", "A"),
                            customer(2L, "B", "B", "B")
                    )),
                    model().attribute("products", contains(
                            product(1L, "A", "A", 10, "1.00"),
                            product(2L, "B", "B", 20, "2.00")
                    )),
                    model().attribute("mode", "create"),
                    view().name("order/order-form")
            );
        }

        @ParameterizedTest
        @ArgumentsSource(RequestParametersProvider.class)
        void doNotCreateOrderWithInvalidFields(Map<String, List<String>> params) throws Exception {
            // when
            var result = client.perform(post("/orders/create")
                    .params(new LinkedMultiValueMap<>(params))
                    .with(csrf())
            );
            // then
            result.andExpect(status().isBadRequest());
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

        @ParameterizedTest
        @EnumSource(OrderForm.StatusForm.class)
        void listOrders(OrderForm.StatusForm status) throws Exception {
            // given
            var pageable = PageRequest.of(0, 8, Sort.by("date"));
            var orderPage = new PageImpl<>(orders, pageable, 3);
            when(orderService.listOrders(any(Order.Status.class), anyInt())).thenReturn(orderPage);
            // when
            var result = client.perform(get("/orders/list")
                    .param("status", status.name())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    request().sessionAttribute("status", status),
                    model().attribute("orders", contains(
                            order("A", LocalDate.now(), 15, "25.00"),
                            order("A", LocalDate.now(), 15, "25.00"),
                            order("A", LocalDate.now(), 15, "25.00")
                    )),
                    model().attribute("currentPage", 1),
                    model().attribute("totalPages", 1),
                    view().name("order/order-table")
            );
            verify(orderService, times(1)).listOrders(any(Order.Status.class), anyInt());
        }

    }

    @Nested
    class FindOrdersTests {

        @Test
        void findOrders() throws Exception {
            var orders = List.of(
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
            when(orderService.findOrders(any(Order.Status.class), anyString())).thenReturn(orders);
            // when
            var result = client.perform(get("/orders/find")
                    .param("status", "UNPAID")
                    .param("customer-name", "A")
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("orders", contains(
                            order("A", LocalDate.now(), 15, "25.00"),
                            order("A", LocalDate.now(), 15, "25.00")
                    )),
                    view().name("order/order-table")
            );
            verify(orderService, times(1)).findOrders(any(Order.Status.class), anyString());
        }

    }

    @Nested
    class UpdateOrderTests {

        @Test
        void retrieveUpdateOrderPage() throws Exception {
            // given
            var order = new OrderBuilder()
                    .id(1L)
                    .status(Order.Status.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .build();
            when(orderService.findOrder(anyLong())).thenReturn(order);
            when(customerService.listCustomers()).thenReturn(List.of(customerA, customerB));
            when(productService.listProducts(any(User.class))).thenReturn(List.of(productA, productB));
            // when
            var result = client.perform(get("/orders/update/{id}", 1L));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("order", is(
                            order("UNPAID", 1L, contains(item(5, 1L)))
                    )),
                    model().attribute("id", 1L),
                    model().attribute("customers", contains(
                            customer(1L, "A", "A", "A"),
                            customer(2L, "B", "B", "B")
                    )),
                    model().attribute("products", contains(
                            product(1L, "A", "A", 10, "1.00"),
                            product(2L, "B", "B", 20, "2.00")
                    )),
                    model().attribute("mode", "update"),
                    view().name("order/order-form")
            );
        }

        @ParameterizedTest
        @ValueSource(strings = { "UNPAID", "PAID" })
        void updateOrder(String sessionStatus) throws Exception {
            // when
            var result = client.perform(post("/orders/update/{id}", 1L)
                    .param("status", "PAID")
                    .param("customerId", "2")
                    .param("items[0].quantity", "10")
                    .param("items[0].productId", "2")
                    .sessionAttr("status", sessionStatus)
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrlTemplate("/orders/list?status={status}", sessionStatus)
            );
            verify(orderService, times(1)).updateOrder(anyLong(), any(Order.class));
        }

        @ParameterizedTest
        @ArgumentsSource(AttributeNameAndExceptionProvider.class)
        void doNotUpdateOrderUsingInvalidItems(String attributeName, Class<? extends RuntimeException> exception) throws Exception {
            // given
            when(customerService.listCustomers()).thenReturn(List.of(customerA, customerB));
            when(productService.listProducts(any(User.class))).thenReturn(List.of(productA, productB));
            doThrow(exception).when(orderService).updateOrder(anyLong(), any(Order.class));
            // when
            var result = client.perform(post("/orders/update/{id}", 1L)
                    .param("status", "PAID")
                    .param("customerId", "2")
                    .param("items[0].quantity", "10")
                    .param("items[0].productId", "2")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute(attributeName, true),
                    model().attribute("order", is(
                            order("PAID", 2L, contains(item(10, 2L)))
                    )),
                    model().attribute("id", 1L),
                    model().attribute("customers", contains(
                            customer(1L, "A", "A", "A"),
                            customer(2L, "B", "B", "B")
                    )),
                    model().attribute("products", contains(
                            product(1L, "A", "A", 10, "1.00"),
                            product(2L, "B", "B", 20, "2.00")
                    )),
                    model().attribute("mode", "update"),
                    view().name("order/order-form")
            );
            verify(orderService, times(1)).updateOrder(anyLong(), any(Order.class));
        }

        @ParameterizedTest
        @ArgumentsSource(RequestParametersProvider.class)
        void doNotUpdateOrderUsingInvalidFields(Map<String, List<String>> params) throws Exception {
            // when
            var result = client.perform(post("/orders/update/{id}", 1L)
                    .params(new LinkedMultiValueMap<>(params))
                    .with(csrf())
            );
            // then
            result.andExpect(status().isBadRequest());
        }

    }

    @Nested
    class DeleteOrderTests {

        @ParameterizedTest
        @ValueSource(strings = { "UNPAID", "PAID" })
        void deleteOrder(String sessionStatus) throws Exception {
            // when
            var result = client.perform(post("/orders/delete/{id}", 1L)
                    .sessionAttr("status", sessionStatus)
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrlTemplate("/orders/list?status={status}", sessionStatus)
            );
            verify(orderService, times(1)).deleteOrder(anyLong());
        }

    }

    static class AttributeNameAndExceptionProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
                    arguments("insufficientStock", ProductWithInsufficientStockException.class),
                    arguments("duplicatedItem", DuplicatedOrderItemException.class)
            );
        }

    }

    static class RequestParametersProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
            return Stream.of(
                    arguments(Map.of(
                            "status", List.of(""),
                            "customerId", List.of(""),
                            "items[0].quantity", List.of(""),
                            "item[0].productId", List.of("")
                    )),
                    arguments(Map.of(
                            "status", List.of("UNPAID"),
                            "customerId", List.of("1"),
                            "items[0].quantity", List.of("0"),
                            "item[0].productId", List.of("1")
                    )),
                    arguments(Map.of(
                            "status", List.of("UNPAID"),
                            "customerId", List.of("1")
                    ))
            );
        }

    }

}
