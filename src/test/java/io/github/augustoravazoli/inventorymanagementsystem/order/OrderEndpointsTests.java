package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.TestApplication;
import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import io.github.augustoravazoli.inventorymanagementsystem.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Import(TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithUserDetails(value = "user@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class OrderEndpointsTests {

    @Autowired
    private MockMvc client;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    private Customer customerA;
    private Customer customerB;
    private Product productA;
    private Product productB;
    private Product productC;
    private Product productD;
    private User user;

    @BeforeEach
    void setup() {
        user = userRepository.save(new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO"));
        customerA = customerRepository.save(new Customer("A", "A", "A", user));
        customerB = customerRepository.save(new Customer("B", "B", "B", user));
        productA = productRepository.save(new Product("A", categoryRepository.save(new Category("A", user)), 10, "1.00", user));
        productB = productRepository.save(new Product("B", categoryRepository.save(new Category("B", user)), 20, "2.00", user));
        productC = productRepository.save(new Product("C", categoryRepository.save(new Category("C", user)), 30, "3.00", user));
        productD = productRepository.save(new Product("D", categoryRepository.save(new Category("D", user)), 40, "4.00", user));
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class CreateOrderTests {

        @Test
        void createOrder() throws Exception {
            // when
            var result = client.perform(post("/orders/create")
                    .param("status", "UNPAID")
                    .param("customerId", customerA.getId().toString())
                    .param("items[0].quantity", "5")
                    .param("items[0].productId", productA.getId().toString())
                    .param("items[1].quantity", "10")
                    .param("items[1].productId", productB.getId().toString())
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/orders/list?status=UNPAID")
            );
            var order = orderRepository.findAllWithItems().getFirst();
            assertThat(order)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "number", "items.id", "items.index", "items.order", "items.product.quantity")
                    .isEqualTo(new OrderBuilder()
                            .status(OrderStatus.UNPAID)
                            .date(LocalDate.now())
                            .customer(customerA)
                            .item(5, productA)
                            .item(10, productB)
                            .owner(user)
                            .build()
                    );
            assertThat(order.getItems()).extracting("product.quantity")
                    .containsExactly(5, 10);
        }

    }

    @Nested
    class ListOrdersTests {

        @Test
        void listOrders() throws Exception {
            // given
            orderRepository.saveAll(List.of(
                    new OrderBuilder()
                            .status(OrderStatus.UNPAID)
                            .customer(customerA)
                            .item(5, productA)
                            .item(10, productB)
                            .owner(user)
                            .build(),
                    new OrderBuilder()
                            .status(OrderStatus.UNPAID)
                            .customer(customerA)
                            .item(5, productA)
                            .item(10, productB)
                            .owner(user)
                            .build(),
                    new OrderBuilder()
                            .status(OrderStatus.UNPAID)
                            .customer(customerA)
                            .item(5, productA)
                            .item(10, productB)
                            .owner(user)
                            .build()
            ));
            // when
            var result = client.perform(get("/orders/list")
                    .param("status", "UNPAID")
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("orders", hasSize(3)),
                    view().name("order/order-table")
            );
        }

    }

    @Nested
    class FindOrdersTests {

        @Test
        void findOrders() throws Exception {
            // given
            orderRepository.saveAll(List.of(
                    new OrderBuilder()
                            .status(OrderStatus.UNPAID)
                            .customer(customerA)
                            .item(5, productA)
                            .item(10, productB)
                            .owner(user)
                            .build(),
                    new OrderBuilder()
                            .status(OrderStatus.UNPAID)
                            .customer(customerA)
                            .item(5, productA)
                            .item(10, productB)
                            .owner(user)
                            .build()
            ));
            // when
            var result = client.perform(get("/orders/find")
                    .param("status", "UNPAID")
                    .param("customer-name", "A")
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("orders", hasSize(2)),
                    view().name("order/order-table")
            );
        }

    }

    @Nested
    class PrintOrderTests {

        @Test
        void printOrder() throws Exception {
            // given
            var order = orderRepository.save(new OrderBuilder()
                    .status(OrderStatus.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(10, productB)
                    .owner(user)
                    .build()
            );
            // when
            var result = client.perform(get("/orders/print/{id}", order.getId()));
            // then
            result.andExpectAll(
                    status().isOk(),
                    content().contentType(MediaType.APPLICATION_PDF),
                    header().exists("Content-Disposition")
            );
        }

    }

    @Nested
    class UpdateOrderTests {

        @Test
        void updateOrder() throws Exception {
            // given
            productA.setQuantity(5);
            productB.setQuantity(12);
            productC.setQuantity(15);
            productRepository.saveAll(List.of(productA, productB, productC));
            var id = orderRepository.save(new OrderBuilder()
                    .status(OrderStatus.UNPAID)
                    .customer(customerA)
                    .item(5, productA)
                    .item(8, productB)
                    .item(15, productC)
                    .owner(user)
                    .build())
                    .getId();
            // when
            var result = client.perform(post("/orders/update/{id}", id)
                    .param("status", "PAID")
                    .param("customerId", customerB.getId().toString())
                    .param("items[0].quantity", "3")
                    .param("items[0].productId", productA.getId().toString())
                    .param("items[1].quantity", "10")
                    .param("items[1].productId", productB.getId().toString())
                    .param("items[2].quantity", "20")
                    .param("items[2].productId", productD.getId().toString())
                    .sessionAttr("status", "UNPAID")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrlTemplate("/orders/list?status=UNPAID")
            );
            var order = orderRepository.findAllWithItems().getFirst();
            assertThat(order)
                    .usingRecursiveComparison()
                    .ignoringFields("id", "number", "date", "items.id", "items.index", "items.order", "items.product.quantity")
                    .isEqualTo(new OrderBuilder()
                            .status(OrderStatus.PAID)
                            .customer(customerB)
                            .item(3, productA)
                            .item(10, productB)
                            .item(20, productD)
                            .owner(user)
                            .build()
                    );
            assertThat(productRepository.findAll(Sort.by("name")))
                    .extracting("quantity")
                    .containsExactly(7, 10, 30, 20);
        }

    }

    @Nested
    class DeleteOrderTests {

        @Test
        void deleteOrder() throws Exception {
            // given
            productA.setQuantity(5);
            productB.setQuantity(12);
            productC.setQuantity(15);
            productRepository.saveAll(List.of(productA, productB, productC));
            var id = orderRepository.save(new OrderBuilder()
                            .status(OrderStatus.UNPAID)
                            .customer(customerA)
                            .item(5, productA)
                            .item(8, productB)
                            .item(15, productC)
                            .owner(user)
                            .build())
                            .getId();
            // when
            var result = client.perform(post("/orders/delete/{id}", id)
                    .sessionAttr("status", "UNPAID")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/orders/list?status=UNPAID")
            );
            assertThat(orderRepository.existsById(id)).isFalse();
            assertThat(customerRepository.existsById(customerA.getId())).isTrue();
            assertThat(productRepository.existsById(productA.getId())).isTrue();
            assertThat(productRepository.existsById(productB.getId())).isTrue();
            assertThat(productRepository.findAll(Sort.by("name")))
                    .extracting("quantity")
                    .containsExactly(10, 20, 30, 40);
        }

    }

}
