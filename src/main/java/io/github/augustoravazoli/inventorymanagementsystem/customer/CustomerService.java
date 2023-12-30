package io.github.augustoravazoli.inventorymanagementsystem.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public List<Customer> listCustomers() {
        return customerRepository.findAll(Sort.by("name"));
    }

    public Customer findCustomer(long id) {
        return customerRepository.findById(id)
                .orElseThrow(CustomerNotFoundException::new);
    }

    public void updateCustomer(long id, Customer updatedCustomer) {
        var customer = customerRepository.findById(id)
                .orElseThrow(CustomerNotFoundException::new);
        if (!customer.getName().equals(updatedCustomer.getName())
            && customerRepository.existsByName(updatedCustomer.getName())) {
            throw new CustomerNameTakenException();
        }
        customer.setName(updatedCustomer.getName());
        customer.setAddress(updatedCustomer.getAddress());
        customer.setPhone(updatedCustomer.getPhone());
        customerRepository.save(customer);
    }

    public void deleteCustomer(long id) {
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException();
        }
        customerRepository.deleteById(id);
    }

}
