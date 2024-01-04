package io.github.augustoravazoli.inventorymanagementsystem.customer;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@WithMockUser
class CustomerControllerTest {

    @MockBean
    private CustomerService customerService;

    @Autowired
    private MockMvc client;

    @Nested
    class CreateCustomerTests {

        @Test
        void retrieveCreateCustomerPage() throws Exception {
            // when
            var result = client.perform(get("/customers/create"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("customer", allOf(
                            hasProperty("name"),
                            hasProperty("address"),
                            hasProperty("phone")
                    )),
                    model().attribute("mode", "create"),
                    view().name("customer/customer-form")
            );
        }

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
            verify(customerService, times(1)).createCustomer(any(Customer.class));
        }

        @Test
        void doNotCreateCustomerWithNameTaken() throws Exception {
            // given
            doThrow(CustomerNameTakenException.class).when(customerService).createCustomer(any(Customer.class));
            // when
            var result = client.perform(post("/customers/create")
                    .param("name", "A")
                    .param("address", "A")
                    .param("phone", "A")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("duplicatedName", true),
                    model().attribute("customer", allOf(
                            hasProperty("name", is("A")),
                            hasProperty("address", is("A")),
                            hasProperty("phone", is("A"))
                    )),
                    model().attribute("mode", "create"),
                    view().name("customer/customer-form")
            );
            verify(customerService, times(1)).createCustomer(any(Customer.class));
        }

        @Test
        void doNotCreateCustomerWithBlankFields() throws Exception {
            // when
            var result = client.perform(post("/customers/create")
                    .param("name", "")
                    .param("address", "")
                    .param("phone", "")
                    .with(csrf())
            );
            // then
            result.andExpect(status().isBadRequest());
        }

    }

    @Nested
    class ListCustomersTests {

        @Test
        void listCustomers() throws Exception {
            // given
            when(customerService.listCustomers(1)).thenReturn(new PageImpl<>(
                    List.of(
                            new Customer("A", "A", "A"),
                            new Customer("B", "B", "B"),
                            new Customer("C", "C", "C")
                    ),
                    PageRequest.of(0, 8, Sort.by("name")),
                    3
            ));
            // when
            var result = client.perform(get("/customers/list"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("customers", contains(
                            customer("A", "A", "A"),
                            customer("B", "B", "B"),
                            customer("C", "C", "C")
                    )),
                    model().attribute("currentPage", 1),
                    model().attribute("totalPages", 1),
                    view().name("customer/customer-table")
            );
            verify(customerService, times(1)).listCustomers(anyInt());
        }

    }

    @Nested
    class UpdateCustomerTests {

        @Test
        void retrieveUpdateCustomerPage() throws Exception {
            // given
            var customer = new Customer(1L, "A", "A", "A");
            when(customerService.findCustomer(1L)).thenReturn(customer);
            // when
            var result = client.perform(get("/customers/update/{id}", 1));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("customer", customer("A", "A", "A")),
                    model().attribute("id", 1L),
                    model().attribute("mode", "update"),
                    view().name("customer/customer-form")
            );
        }

        @Test
        void updateCustomer() throws Exception {
            // when
            var result = client.perform(post("/customers/update/{id}", 1L)
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
            verify(customerService, times(1)).updateCustomer(anyLong(), any(Customer.class));
        }

        @Test
        void doNotUpdateCustomerUsingNameTaken() throws Exception {
            // given
            doThrow(CustomerNameTakenException.class).when(customerService).updateCustomer(anyLong(), any(Customer.class));
            // when
            var result = client.perform(post("/customers/update/{id}", 1L)
                    .param("name", "B")
                    .param("address", "B")
                    .param("phone", "B")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("duplicatedName", true),
                    model().attribute("customer", customer("B", "B", "B")),
                    model().attribute("id", 1L),
                    model().attribute("mode", "update"),
                    view().name("customer/customer-form")
            );
            verify(customerService, times(1)).updateCustomer(anyLong(), any(Customer.class));
        }

        @Test
        void doNotUpdateCustomerUsingBlankFields() throws Exception {
            // when
            var result = client.perform(post("/customers/update/{id}", 1L)
                    .param("name", "")
                    .param("address", "")
                    .param("phone", "")
                    .with(csrf())
            );
            // then
            result.andExpect(status().isBadRequest());
        }

    }

    private Matcher<Customer> customer(String name, String address, String phone) {
        return allOf(
                hasProperty("name", is(name)),
                hasProperty("address", is(address)),
                hasProperty("phone", is(phone))
        );
    }

}