package io.github.augustoravazoli.inventorymanagementsystem.customer;

import io.github.augustoravazoli.inventorymanagementsystem.order.OrderRepository;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public CustomerService(CustomerRepository customerRepository, OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void createCustomer(Customer customer, User owner) {
        if (customerRepository.existsByNameAndOwner(customer.getName(), owner)) {
            logger.info("Customer name {} of user {} already in use, throwing exception", customer.getName(), owner.getEmail());
            throw new CustomerNameTakenException();
        }
        customer.setOwner(owner);
        customerRepository.save(customer);
        logger.info("Customer {} created for user {}", customer.getName(), owner.getEmail());
    }

    @Transactional(readOnly = true)
    public Page<Customer> listCustomers(int page, User owner) {
        logger.info("Listing customers paginated for user {}", owner.getEmail());
        return customerRepository.findAllByOwner(owner, PageRequest.of(page - 1, 8, Sort.by("name")));
    }

    @Transactional(readOnly = true)
    public List<Customer> listCustomers(User owner) {
        logger.info("Listing customers for user {}", owner.getEmail());
        return customerRepository.findAllByOwner(owner, Sort.by("name"));
    }

    @Transactional(readOnly = true)
    public List<Customer> findCustomers(String name, User owner) {
        logger.info("Finding customers containing name {} for user {}", name, owner.getEmail());
        return customerRepository.findAllByNameContainingIgnoreCaseAndOwner(name, owner);
    }

    @Transactional(readOnly = true)
    public Customer findCustomer(long id, User owner) {
        logger.info("Finding customer with id {} for user {}", id, owner.getEmail());
        return customerRepository.findByIdAndOwner(id, owner)
                .orElseThrow(CustomerNotFoundException::new);
    }

    @Transactional
    public void updateCustomer(long id, Customer updatedCustomer, User owner) {
        var customer = customerRepository.findByIdAndOwner(id, owner)
                .orElseThrow(CustomerNotFoundException::new);
        if (!customer.getName().equals(updatedCustomer.getName())
            && customerRepository.existsByNameAndOwner(updatedCustomer.getName(), owner)) {
            logger.info("New customer name {} of user {} already in use, throwing exception", updatedCustomer.getName(), owner.getEmail());
            throw new CustomerNameTakenException();
        }
        customer.setName(updatedCustomer.getName());
        customer.setAddress(updatedCustomer.getAddress());
        customer.setPhone(updatedCustomer.getPhone());
        customerRepository.save(customer);
        logger.info("Customer with id {} of user {} updated, new name is {}", customer.getId(), owner.getEmail(), updatedCustomer.getName());
    }

    @Transactional
    public void deleteCustomer(long id, User owner) {
        if (!customerRepository.existsByIdAndOwner(id, owner)) {
            logger.info("Customer with id {} of user {} not found, throwing exception", id, owner.getEmail());
            throw new CustomerNotFoundException();
        }
        if (orderRepository.existsByCustomerIdAndOwner(id, owner)) {
            logger.info("Customer with id {} of user {} still has orders, throwing exception", id, owner.getEmail());
            throw new CustomerDeletionNotAllowedException();
        }
        customerRepository.deleteById(id);
        logger.info("Customer with id {} of user {} deleted", id, owner.getEmail());
    }

}
