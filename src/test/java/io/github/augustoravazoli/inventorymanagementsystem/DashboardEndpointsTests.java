package io.github.augustoravazoli.inventorymanagementsystem;

import io.github.augustoravazoli.inventorymanagementsystem.category.Category;
import io.github.augustoravazoli.inventorymanagementsystem.category.CategoryRepository;
import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.order.Order;
import io.github.augustoravazoli.inventorymanagementsystem.order.OrderBuilder;
import io.github.augustoravazoli.inventorymanagementsystem.order.OrderRepository;
import io.github.augustoravazoli.inventorymanagementsystem.product.Product;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import io.github.augustoravazoli.inventorymanagementsystem.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithUserDetails(value = "user@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class DashboardEndpointsTests {

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

    @BeforeEach
    void setup() {
        var user = userRepository.save(new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO"));
        var customerA = customerRepository.save(new Customer("A", "A", "A", user));
        var customerB = customerRepository.save(new Customer("B", "B", "B", user));
        var productA = productRepository.save(new Product("A", categoryRepository.save(new Category("A", user)), 10, "1.00", user));
        var productB = productRepository.save(new Product("B", categoryRepository.save(new Category("B", user)), 20, "2.00", user));
        var productC = productRepository.save(new Product("C", categoryRepository.save(new Category("C", user)), 30, "3.00", user));
        orderRepository.saveAll(List.of(
                new OrderBuilder()
                        .status(Order.Status.UNPAID)
                        .customer(customerA)
                        .item(5, productA)
                        .item(10, productB)
                        .build(),
                new OrderBuilder()
                        .status(Order.Status.PAID)
                        .customer(customerB)
                        .item(3, productA)
                        .item(8, productB)
                        .build(),
                new OrderBuilder()
                        .status(Order.Status.PAID)
                        .customer(customerB)
                        .item(2, productA)
                        .item(5, productC)
                        .build()
        ));
    }

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void retrieveDashboard() throws Exception {
        // when
        var result = client.perform(get("/dashboard"));
        // then
        result.andExpectAll(
                status().isOk(),
                model().attribute("totalCustomers", 2L),
                model().attribute("totalCategories", 3L),
                model().attribute("totalProducts", 3L),
                model().attribute("totalUnpaidOrders", 1L),
                model().attribute("totalPaidOrders", 2L),
                model().attribute("totalSales", new BigDecimal("36.00")),
                view().name("dashboard")
        );
    }

}
