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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
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
@WithMockUser
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

    @BeforeEach
    void setup() {
        var customerA = customerRepository.save(new Customer("A", "A", "A"));
        var customerB = customerRepository.save(new Customer("B", "B", "B"));
        var productA = productRepository.save(new Product("A", categoryRepository.save(new Category("A")), 10, "1.00"));
        var productB = productRepository.save(new Product("B", categoryRepository.save(new Category("B")), 20, "2.00"));
        var productC = productRepository.save(new Product("C", categoryRepository.save(new Category("C")), 30, "3.00"));
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
