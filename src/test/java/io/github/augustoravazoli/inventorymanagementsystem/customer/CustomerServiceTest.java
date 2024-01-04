package io.github.augustoravazoli.inventorymanagementsystem.customer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Nested
    class CreateCustomerTests {

        @Test
        void createCustomer() {
            // given
            var customer = new Customer("A", "A", "A");
            when(customerRepository.existsByName("A")).thenReturn(false);
            // when
            customerService.createCustomer(customer);
            // then
            verify(customerRepository, times(1)).save(customer);
        }

        @Test
        void doNotCreateCustomerWithNameTaken() {
            // given
            var customer = new Customer("A", "A", "A");
            when(customerRepository.existsByName("A")).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> customerService.createCustomer(customer));
            // then
            exception.isInstanceOf(CustomerNameTakenException.class);
            verify(customerRepository, never()).save(customer);
        }

    }

    @Nested
    class ListCustomersTests {

        private final List<Customer> customers = List.of(
                new Customer("A", "A", "A"),
                new Customer("B", "B", "B"),
                new Customer("C", "C", "C")
        );

        @Test
        void listCustomersPaginated() {
            // given
            var pageable = PageRequest.of(0, 8, Sort.by("name"));
            var expectedCustomerPage = new PageImpl<>(customers, pageable, 3);
            // when
            when(customerRepository.findAll(pageable)).thenReturn(expectedCustomerPage);
            // when
            var actualCustomerPage = customerService.listCustomers(1);
            // then
            assertThat(actualCustomerPage.getContent()).extracting("name").isSorted();
            assertThat(actualCustomerPage).usingRecursiveComparison().isEqualTo(expectedCustomerPage);
        }

        @Test
        void listCustomers() {
            // given
            var expectedCustomers = customers;
            when(customerRepository.findAll(Sort.by("name"))).thenReturn(expectedCustomers);
            // when
            var actualCustomers = customerService.listCustomers();
            // then
            assertThat(actualCustomers).extracting("name").isSorted();
            assertThat(actualCustomers).usingRecursiveComparison().isEqualTo(expectedCustomers);
        }

    }

    @Nested
    class FindCustomerTests {

        @Test
        void findCustomer() {
            // given
            var expectedCustomer = new Customer(1L, "A", "A", "A");
            when(customerRepository.findById(1L)).thenReturn(Optional.of(expectedCustomer));
            // when
            var actualCustomer = customerService.findCustomer(1L);
            // then
            assertThat(actualCustomer).usingRecursiveComparison().isEqualTo(expectedCustomer);
        }

        @Test
        void doNotFindCustomerThatDoesNotExists() {
            // given
            when(customerRepository.findById(1L)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> customerService.findCustomer(1L));
            // then
            exception.isInstanceOf(CustomerNotFoundException.class);
        }

    }

    @Nested
    class UpdateCustomersTests {

        @Test
        void updateCustomer() {
            // given
            var customer = new Customer("A", "A", "A");
            var updatedCustomer = new Customer("B", "B", "B");
            when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(customerRepository.existsByName("B")).thenReturn(false);
            // when
            customerService.updateCustomer(1L, updatedCustomer);
            // then
            assertThat(customer).usingRecursiveComparison().isEqualTo(updatedCustomer);
            verify(customerRepository, times(1)).save(customer);
        }

        @Test
        void doNotUpdateCustomerThatDoesNotExists() {
            // given
            var updatedCustomer = new Customer("B", "B", "B");
            when(customerRepository.findById(1L)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> customerService.updateCustomer(1L, updatedCustomer));
            // then
            exception.isInstanceOf(CustomerNotFoundException.class);
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        void doNotUpdateCustomerUsingNameTaken() {
            // given
            var customer = new Customer("A", "A", "A");
            var updatedCustomer = new Customer("B", "B", "B");
            when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(customerRepository.existsByName("B")).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> customerService.updateCustomer(1L, updatedCustomer));
            // then
            exception.isInstanceOf(CustomerNameTakenException.class);
            verify(customerRepository, never()).save(any(Customer.class));
        }

    }

    @Nested
    class DeleteCustomerTests {

        @Test
        void deleteCustomer() {
            // given
            when(customerRepository.existsById(1L)).thenReturn(true);
            // when
            customerService.deleteCustomer(1L);
            // then
            verify(customerRepository, times(1)).deleteById(1L);
        }

        @Test
        void doNotDeleteCustomerThatDoesNotExists() {
            // given
            when(customerRepository.existsById(1L)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> customerService.deleteCustomer(1L));
            // then
            exception.isInstanceOf(CustomerNotFoundException.class);
            verify(customerRepository, never()).deleteById(1L);
        }

    }

}
