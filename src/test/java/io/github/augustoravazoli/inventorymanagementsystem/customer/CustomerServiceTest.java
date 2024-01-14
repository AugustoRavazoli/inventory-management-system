package io.github.augustoravazoli.inventorymanagementsystem.customer;

import io.github.augustoravazoli.inventorymanagementsystem.order.OrderRepository;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
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

    @Mock
    private OrderRepository orderRepository;

    private final User user = new User();

    @Nested
    class CreateCustomerTests {

        @Test
        void createCustomer() {
            // given
            var customer = new Customer("A", "A", "A");
            when(customerRepository.existsByNameAndOwner("A", user)).thenReturn(false);
            // when
            customerService.createCustomer(customer, user);
            // then
            assertThat(customer.getOwner()).isEqualTo(user);
            verify(customerRepository, times(1)).save(customer);
        }

        @Test
        void doNotCreateCustomerWithNameTaken() {
            // given
            var customer = new Customer("A", "A", "A");
            when(customerRepository.existsByNameAndOwner("A", user)).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> customerService.createCustomer(customer, user));
            // then
            exception.isInstanceOf(CustomerNameTakenException.class);
            verify(customerRepository, never()).save(any(Customer.class));
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
            when(customerRepository.findAllByOwner(user, pageable)).thenReturn(expectedCustomerPage);
            // when
            var actualCustomerPage = customerService.listCustomers(1, user);
            // then
            assertThat(actualCustomerPage.getContent()).extracting("name").isSorted();
            assertThat(actualCustomerPage).usingRecursiveComparison().isEqualTo(expectedCustomerPage);
        }

        @Test
        void listCustomers() {
            // given
            var expectedCustomers = customers;
            when(customerRepository.findAllByOwner(user, Sort.by("name"))).thenReturn(expectedCustomers);
            // when
            var actualCustomers = customerService.listCustomers(user);
            // then
            assertThat(actualCustomers).extracting("name").isSorted();
            assertThat(actualCustomers).usingRecursiveComparison().isEqualTo(expectedCustomers);
        }

    }

    @Nested
    class FindCustomersTests {

        @Test
        void findCustomers() {
            // given
            var expectedCustomers = List.of(
                    new Customer("A", "A", "A"),
                    new Customer("Aa", "Aa", "Aa")
            );
            when(customerRepository.findAllByNameContainingIgnoreCaseAndOwner("A", user)).thenReturn(expectedCustomers);
            // when
            var actualCustomers = customerService.findCustomers("A", user);
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
            when(customerRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(expectedCustomer));
            // when
            var actualCustomer = customerService.findCustomer(1L, user);
            // then
            assertThat(actualCustomer).usingRecursiveComparison().isEqualTo(expectedCustomer);
        }

        @Test
        void doNotFindCustomerThatDoesNotExists() {
            // given
            when(customerRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> customerService.findCustomer(1L, user));
            // then
            exception.isInstanceOf(CustomerNotFoundException.class);
        }

    }

    @Nested
    class UpdateCustomerTests {

        @Test
        void updateCustomer() {
            // given
            var customer = new Customer("A", "A", "A", user);
            var updatedCustomer = new Customer("B", "B", "B");
            when(customerRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(customer));
            when(customerRepository.existsByNameAndOwner("B", user)).thenReturn(false);
            // when
            customerService.updateCustomer(1L, updatedCustomer, user);
            // then
            assertThat(customer).usingRecursiveComparison().ignoringFields("owner").isEqualTo(updatedCustomer);
            assertThat(customer.getOwner()).isEqualTo(user);
            verify(customerRepository, times(1)).save(customer);
        }

        @Test
        void doNotUpdateCustomerThatDoesNotExists() {
            // given
            var updatedCustomer = new Customer("B", "B", "B");
            when(customerRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> customerService.updateCustomer(1L, updatedCustomer, user));
            // then
            exception.isInstanceOf(CustomerNotFoundException.class);
            verify(customerRepository, never()).save(any(Customer.class));
        }

        @Test
        void doNotUpdateCustomerUsingNameTaken() {
            // given
            var customer = new Customer("A", "A", "A");
            var updatedCustomer = new Customer("B", "B", "B");
            when(customerRepository.findByIdAndOwner(1L, user)).thenReturn(Optional.of(customer));
            when(customerRepository.existsByNameAndOwner("B", user)).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> customerService.updateCustomer(1L, updatedCustomer, user));
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
            when(customerRepository.existsByIdAndOwner(1L, user)).thenReturn(true);
            when(orderRepository.existsByCustomerIdAndOwner(1L, user)).thenReturn(false);
            // when
            customerService.deleteCustomer(1L, user);
            // then
            verify(customerRepository, times(1)).deleteById(1L);
        }

        @Test
        void doNotDeleteCustomerThatDoesNotExists() {
            // given
            when(customerRepository.existsByIdAndOwner(1L, user)).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> customerService.deleteCustomer(1L, user));
            // then
            exception.isInstanceOf(CustomerNotFoundException.class);
            verify(customerRepository, never()).deleteById(anyLong());
        }

        @Test
        void doNotDeleteCustomerAssociatedWithOrders() {
            // given
            when(customerRepository.existsByIdAndOwner(1L, user)).thenReturn(true);
            when(orderRepository.existsByCustomerIdAndOwner(1L, user)).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> customerService.deleteCustomer(1L, user));
            // then
            exception.isInstanceOf(CustomerDeletionNotAllowedException.class);
            verify(customerRepository, never()).deleteById(anyLong());
        }

    }

}
