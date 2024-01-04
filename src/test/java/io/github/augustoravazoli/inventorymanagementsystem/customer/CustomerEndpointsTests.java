package io.github.augustoravazoli.inventorymanagementsystem.customer;

import io.github.augustoravazoli.inventorymanagementsystem.TestApplication;
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
@WithMockUser
class CustomerEndpointsTests {

    @Autowired
    private MockMvc client;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setup() {
        customerRepository.deleteAll();
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
            var customerOptional = customerRepository.findByName("A");
            assertThat(customerOptional).get()
                    .hasFieldOrPropertyWithValue("name", "A")
                    .hasFieldOrPropertyWithValue("address", "A")
                    .hasFieldOrPropertyWithValue("phone", "A");
        }

    }

    @Nested
    class ListCustomersTests {

        @Test
        void listCustomers() throws Exception {
            // given
            customerRepository.saveAll(List.of(
                    new Customer("A", "A", "A"),
                    new Customer("B", "B", "B"),
                    new Customer("C", "C", "C")
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
    class UpdateCustomerTests {

        @Test
        void updateCustomer() throws Exception {
            // given
            var id = customerRepository.save(new Customer("A", "A", "A")).getId();
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
                    .hasFieldOrPropertyWithValue("name", "B")
                    .hasFieldOrPropertyWithValue("address", "B")
                    .hasFieldOrPropertyWithValue("phone", "B");
        }

    }

    @Nested
    class DeleteCustomerTests {

        @Test
        void deleteCustomer() throws Exception {
            // given
            var id = customerRepository.save(new Customer("A", "A", "A")).getId();
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
