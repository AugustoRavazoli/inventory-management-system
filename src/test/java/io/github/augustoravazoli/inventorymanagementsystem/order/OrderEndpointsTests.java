package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.TestApplication;
import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithMockUser
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

    private Customer customer;
    private Product productA;
    private Product productB;

    @BeforeEach
    void setup() {
        customer = customerRepository.save(new Customer("A", "A", "A"));
        productA = productRepository.save(new Product("A", categoryRepository.save(new Category("A")), 10, "1.00"));
        productB = productRepository.save(new Product("B", categoryRepository.save(new Category("B")), 20, "2.00"));
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        customerRepository.deleteAll();
    }

    @Nested
    class CreateOrderTests {

        @Test
        void createOrder() throws Exception {
            // when
            var result = client.perform(post("/orders/create")
                    .param("status", "UNPAID")
                    .param("customerId", customer.getId().toString())
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
                    .ignoringFields("id", "items.id", "items.product.quantity")
                    .isEqualTo(new OrderBuilder()
                            .status(Order.Status.UNPAID)
                            .date(LocalDate.now())
                            .customer(customer)
                            .item(5, productA)
                            .item(10, productB)
                            .build()
                    );
            assertThat(order.getItems()).extracting("product.quantity")
                    .containsExactly(5, 10);
        }

    }

}
