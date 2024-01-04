package io.github.augustoravazoli.inventorymanagementsystem.customer;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

}
