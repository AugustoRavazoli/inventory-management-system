package io.github.augustoravazoli.inventorymanagementsystem.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public void createCustomer(Customer customer) {
        if (customerRepository.existsByName(customer.getName())) {
            throw new CustomerNameTakenException();
        }
        customerRepository.save(customer);
    }

    public Page<Customer> listCustomers(String name, Pageable page) {
        if (name.isEmpty()) {
            return customerRepository.findAll(page);
        }
        return customerRepository.findAllByNameContainingIgnoreCase(name, page);
    }

}
