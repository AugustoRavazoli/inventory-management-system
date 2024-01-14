package io.github.augustoravazoli.inventorymanagementsystem.customer;

import io.github.augustoravazoli.inventorymanagementsystem.MockUserDetailsService;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerMatchers.customer;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@Import(MockUserDetailsService.class)
@WithUserDetails
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
                    model().attribute("customer", is(customer())),
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
            verify(customerService, times(1)).createCustomer(any(Customer.class), any(User.class));
        }

        @Test
        void doNotCreateCustomerWithNameTaken() throws Exception {
            // given
            doThrow(CustomerNameTakenException.class).when(customerService).createCustomer(any(Customer.class), any(User.class));
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
                    model().attribute("customer", is(customer("A", "A", "A"))),
                    model().attribute("mode", "create"),
                    view().name("customer/customer-form")
            );
            verify(customerService, times(1)).createCustomer(any(Customer.class), any(User.class));
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
            var customers = List.of(
                    new Customer("A", "A", "A"),
                    new Customer("B", "B", "B"),
                    new Customer("C", "C", "C")
            );
            var pageable = PageRequest.of(0, 8, Sort.by("name"));
            when(customerService.listCustomers(anyInt(), any(User.class))).thenReturn(new PageImpl<>(customers, pageable, 3));
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
            verify(customerService, times(1)).listCustomers(anyInt(), any(User.class));
        }

    }

    @Nested
    class FindCustomersTests {

        @Test
        void findCustomers() throws Exception {
            // given
            var customers = List.of(
                    new Customer("A", "A", "A"),
                    new Customer("Aa", "Aa", "Aa")
            );
            when(customerService.findCustomers(anyString(), any(User.class))).thenReturn(customers);
            // when
            var result = client.perform(get("/customers/find")
                    .param("name", "A")
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("customers", contains(
                            customer("A", "A", "A"),
                            customer("Aa", "Aa", "Aa")
                    )),
                    view().name("customer/customer-table")
            );
            verify(customerService, times(1)).findCustomers(anyString(), any(User.class));
        }

    }

    @Nested
    class UpdateCustomerTests {

        @Test
        void retrieveUpdateCustomerPage() throws Exception {
            // given
            var customer = new Customer(1L, "A", "A", "A");
            when(customerService.findCustomer(anyLong(), any(User.class))).thenReturn(customer);
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
            verify(customerService, times(1)).updateCustomer(anyLong(), any(Customer.class), any(User.class));
        }

        @Test
        void doNotUpdateCustomerUsingNameTaken() throws Exception {
            // given
            doThrow(CustomerNameTakenException.class).when(customerService).updateCustomer(anyLong(), any(Customer.class), any(User.class));
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
            verify(customerService, times(1)).updateCustomer(anyLong(), any(Customer.class), any(User.class));
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

    @Nested
    class DeleteCustomerTests {

        @Test
        void deleteCustomer() throws Exception {
            // when
            var result = client.perform(post("/customers/delete/{id}", 1L)
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/customers/list")
            );
            verify(customerService, times(1)).deleteCustomer(anyLong(), any(User.class));
        }

        @Test
        void doNotDeleteCustomerAssociatedWithOrders() throws Exception {
            // given
            doThrow(CustomerDeletionNotAllowedException.class).when(customerService).deleteCustomer(anyLong(), any(User.class));
            // when
            var result = client.perform(post("/customers/delete/{id}", 1L)
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    flash().attribute("deleteNotAllowed", true),
                    redirectedUrl("/customers/list")
            );
            verify(customerService, times(1)).deleteCustomer(anyLong(), any(User.class));
        }

    }

}
