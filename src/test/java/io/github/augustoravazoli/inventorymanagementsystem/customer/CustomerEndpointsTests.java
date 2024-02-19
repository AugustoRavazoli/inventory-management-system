package io.github.augustoravazoli.inventorymanagementsystem.customer;

import io.github.augustoravazoli.inventorymanagementsystem.TestApplication;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Import(TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
@WithUserDetails(value = "user@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
class CustomerEndpointsTests {

    @Autowired
    private MockMvc client;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        user = userRepository.save(new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO"));
    }

    @AfterEach
    void tearDown() {
        customerRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class CreateCustomerTests {

        @Test
        void createCustomer() throws Exception {
            // when
            var result = client.perform(post("/customers/create")
                    .param("name", "A")
                    .param("address", "A")
                    .param("phone", "A")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/customers/list")
            );
            var customerOptional = customerRepository.findByNameAndOwner("A", user);
            assertThat(customerOptional).get()
                    .extracting("name", "address", "phone", "owner.email")
                    .containsExactly("A", "A", "A", "user@email.com");
        }

    }

    @Nested
    class CreateAllCustomersTests {

        @Test
        void createAllCustomers() throws Exception {
            // given
            var csv = "name;address;phone;\nA;A;A\nB;B;B\nC;C;C";
            var file = new MockMultipartFile("customers", "customers.csv", "text/csv", csv.getBytes());
            // when
            var result = client.perform(multipart("/customers/create-all")
                    .file(file)
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/customers/list")
            );
            var customers = customerRepository.findAll();
            assertThat(customers).extracting("name", "address", "phone", "owner.email").containsExactly(
                    tuple("A", "A", "A", "user@email.com"),
                    tuple("B", "B", "B", "user@email.com"),
                    tuple("C", "C", "C", "user@email.com")
            );
        }

    }

    @Nested
    class ListCustomersTests {

        @Test
        void listCustomers() throws Exception {
            // given
            customerRepository.saveAll(List.of(
                    new Customer("A", "A", "A", user),
                    new Customer("B", "B", "B", user),
                    new Customer("C", "C", "C", user)
            ));
            // when
            var result = client.perform(get("/customers/list"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("customers", hasSize(3)),
                    view().name("customer/customer-table")
            );
        }

    }

    @Nested
    class FindCustomersTests {

        @Test
        void findCustomers() throws Exception {
            // given
            customerRepository.saveAll(List.of(
                    new Customer("A", "A", "A", user),
                    new Customer("Aa", "Aa", "Aa", user))
            );
            // when
            var result = client.perform(get("/customers/find")
                    .param("name", "A")
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("customers", hasSize(2)),
                    view().name("customer/customer-table")
            );
        }

    }

    @Nested
    class UpdateCustomerTests {

        @Test
        void updateCustomer() throws Exception {
            // given
            var id = customerRepository.save(new Customer("A", "A", "A", user)).getId();
            // when
            var result = client.perform(post("/customers/update/{id}", id)
                    .param("name", "B")
                    .param("address", "B")
                    .param("phone", "B")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/customers/list")
            );
            var customerOptional = customerRepository.findById(id);
            assertThat(customerOptional).get()
                    .extracting("name", "address", "phone", "owner.email")
                    .containsExactly("B", "B", "B", "user@email.com");
        }

    }

    @Nested
    class DeleteCustomerTests {

        @Test
        void deleteCustomer() throws Exception {
            // given
            var id = customerRepository.save(new Customer("A", "A", "A", user)).getId();
            // when
            var result = client.perform(post("/customers/delete/{id}", id)
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/customers/list")
            );
            assertThat(customerRepository.existsById(id)).isFalse();
        }

    }

}
