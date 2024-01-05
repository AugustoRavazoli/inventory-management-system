package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerService;
import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductService;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@WithMockUser
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
            when(productService.listProducts()).thenReturn(List.of(productA, productB));
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

        private static Stream<Arguments> provideAttributeNameAndExceptionClass() {
            return Stream.of(
                    arguments("insufficientStock", ProductWithInsufficientStockException.class),
                    arguments("duplicatedItem", DuplicatedOrderItemException.class)
            );
        }

        @ParameterizedTest
        @MethodSource("provideAttributeNameAndExceptionClass")
        void doNotCreateOrderWithInvalidItems(String attributeName, Class<? extends RuntimeException> exception) throws Exception {
            // given
            when(customerService.listCustomers()).thenReturn(List.of(customerA, customerB));
            when(productService.listProducts()).thenReturn(List.of(productA, productB));
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

        private static Stream<Arguments> provideRequestParameters() {
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

        @ParameterizedTest
        @MethodSource("provideRequestParameters")
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

    private Matcher<OrderForm> order() {
        return allOf(
                hasProperty("status", nullValue()),
                hasProperty("customerId", nullValue()),
                hasProperty("items", nullValue())
        );
    }

    private Matcher<OrderForm> order(String status, Long customerId, Matcher<Iterable<? extends OrderItemForm>> itemsMatcher) {
        return allOf(
                hasProperty("status", is(OrderForm.StatusForm.valueOf(status))),
                hasProperty("customerId", is(customerId)),
                hasProperty("items", itemsMatcher)
        );
    }

    private Matcher<OrderItemForm> item(Integer quantity, Long productId) {
        return allOf(
                hasProperty("quantity", is(quantity)),
                hasProperty("productId", is(productId))
        );
    }

    private Matcher<Customer> customer(Long id, String name, String address, String phone) {
        return allOf(
                hasProperty("id", is(id)),
                hasProperty("name", is(name)),
                hasProperty("address", is(address)),
                hasProperty("phone", is(phone))
        );
    }

    private Matcher<Product> product(Long id, String name, String category, Integer quantity, String price) {
        return allOf(
                hasProperty("id", is(id)),
                hasProperty("name", is(name)),
                hasProperty("category", hasProperty("name", is(category))),
                hasProperty("quantity", is(quantity)),
                hasProperty("price", is(new BigDecimal(price)))
        );
    }

}
