package io.github.augustoravazoli.inventorymanagementsystem.customer;

import io.github.augustoravazoli.inventorymanagementsystem.order.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository,
                           OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public void createCustomer(Customer customer) {
        if (customerRepository.existsByName(customer.getName())) {
            throw new CustomerNameTakenException();
        }
        customerRepository.save(customer);
    }

    public Page<Customer> listCustomers(int page) {
        return customerRepository.findAll(PageRequest.of(page - 1, 8, Sort.by("name")));
    }

    public List<Customer> listCustomers() {
        return customerRepository.findAll(Sort.by("name"));
    }

    public List<Customer> findCustomers(String name) {
        return customerRepository.findAllByNameContainingIgnoreCase(name);
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
        if (orderRepository.existsByCustomerId(id)) {
            throw new CustomerDeletionNotAllowedException();
        }
        customerRepository.deleteById(id);
    }

}
