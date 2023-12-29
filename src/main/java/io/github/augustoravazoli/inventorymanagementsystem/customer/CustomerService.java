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

    public Page<Customer> listCustomers(String name, Pageable page) {
        if (name.isEmpty()) {
            return customerRepository.findAll(page);
        }
        return customerRepository.findAllByNameContainingIgnoreCase(name, page);
    }

}
