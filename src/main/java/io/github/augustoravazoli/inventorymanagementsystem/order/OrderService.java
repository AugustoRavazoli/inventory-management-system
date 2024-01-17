package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public void createOrder(Order order, User owner) {
        checkCustomer(order.getCustomer(), owner);
        checkDuplicates(order.getItems());
        checkProductAvailability(order.getItems(), owner);
        updateProductQuantities(order.getItems(), "decrease", owner);
        order.setOwner(owner);
        orderRepository.save(order);
        logger.info("Order created for customer {} of user {}", order.getCustomer().getName(), owner.getEmail());
    }

    public Page<Order> listOrders(OrderStatus status, int page, User owner) {
        logger.info("Listing {} orders paginated for user {}", status, owner.getEmail());
        return orderRepository.findAllByStatusAndOwner(status, owner, PageRequest.of(page - 1, 8, Sort.by("date")));
    }

    public List<Order> findOrders(OrderStatus status, String customerName, User owner) {
        logger.info("Finding {} orders containing customer name {} for user {}", status, customerName, owner.getEmail());
        return orderRepository.findAllByStatusAndCustomerNameContainingIgnoreCaseAndOwner(status, customerName, owner);
    }

    public Order findOrder(long id, User owner) {
        logger.info("Finding order with id {} for user {}", id, owner.getEmail());
        return orderRepository.findByIdAndOwner(id, owner)
                .orElseThrow(OrderNotFoundException::new);
    }

    @Transactional
    public void updateOrder(long id, Order updatedOrder, User owner) {
        var order = orderRepository.findByIdAndOwner(id, owner).orElseThrow(OrderNotFoundException::new);
        checkCustomer(updatedOrder.getCustomer(), owner);
        checkDuplicates(updatedOrder.getItems());
        checkProductAvailability(updatedOrder.getItems(), owner);
        updateProductQuantitiesForExistingItems(order.getItems(), updatedOrder.getItems(), owner);
        decreaseProductQuantitiesForNewItems(order.getItems(), updatedOrder.getItems(), owner);
        resetProductQuantitiesForRemovedItems(order.getItems(), updatedOrder.getItems(), owner);
        updateOrderDetails(order, updatedOrder);
        orderRepository.save(order);
        logger.info("Order with id {} of user {} updated", order.getId(), owner.getEmail());
    }

    @Transactional
    public void deleteOrder(long id, User owner) {
        var order = orderRepository.findByIdAndOwner(id, owner).orElseThrow(OrderNotFoundException::new);
        if (order.getStatus() == OrderStatus.UNPAID) {
            logger.info("Order status is UNPAID, reset associated product quantities");
            updateProductQuantities(order.getItems(), "increase", owner);
        }
        orderRepository.delete(order);
        logger.info("Order with id {} of user {} deleted", id, owner.getEmail());
    }

    private void checkCustomer(Customer customer, User owner) {
        if (!customerRepository.existsByIdAndOwner(customer.getId(), owner)) {
            logger.info("Customer with id {} not found, throwing exception", customer.getId());
            throw new InvalidCustomerException();
        }
    }

    private void checkDuplicates(List<OrderItem> items) {
        var elements = new HashSet<Long>();
        if (items.stream().anyMatch(item -> !elements.add(item.getProduct().getId()))) {
            logger.info("Order items contains duplicated items, throwing exception");
            throw new DuplicatedOrderItemException();
        }
    }

    private void checkProductAvailability(List<OrderItem> items, User owner) {
        for (var item : items) {
            var product = productRepository.findByIdAndOwner(item.getProduct().getId(), owner)
                    .orElseThrow(InvalidProductException::new);
            if (item.getQuantity() > product.getQuantity()) {
                logger.info("Order items contains products with insufficient stock, throwing exception");
                throw new ProductWithInsufficientStockException();
            }
        }
    }

    private void updateProductQuantities(List<OrderItem> items, String operation, User owner) {
        for (var item : items) {
            var product = productRepository.findByIdAndOwner(item.getProduct().getId(), owner).orElseThrow();
            switch (operation) {
                case "increase" -> product.increaseQuantity(item.getQuantity());
                case "decrease" -> product.decreaseQuantity(item.getQuantity());
                default -> throw new IllegalArgumentException("No case found for operation " + operation);
            }
            logger.info("Updating product {}, new quantity is {}", product.getName(), product.getQuantity());
        }
    }

    private void updateProductQuantitiesForExistingItems(List<OrderItem> items, List<OrderItem> updatedItems, User owner) {
        var existingItems = updatedItems.stream().filter(item -> contains(item, items)).toList();
        var oldItems = items.stream().filter(item -> contains(item, existingItems)).toList();
        updateProductQuantities(oldItems, "increase", owner);
        updateProductQuantities(existingItems, "decrease", owner);
    }

    private void decreaseProductQuantitiesForNewItems(List<OrderItem> items, List<OrderItem> updatedItems, User owner) {
        var newItems = updatedItems.stream().filter(item -> !contains(item, items)).toList();
        updateProductQuantities(newItems, "decrease", owner);
    }

    private void resetProductQuantitiesForRemovedItems(List<OrderItem> items, List<OrderItem> updatedItems, User owner) {
        var removedItems = items.stream().filter(item -> !contains(item, updatedItems)).toList();
        updateProductQuantities(removedItems, "increase", owner);
    }

    private boolean contains(OrderItem item, List<OrderItem> items) {
        return items.stream()
                .anyMatch(i -> i.getProduct().getId().equals(item.getProduct().getId()));
    }

    private void updateOrderDetails(Order order, Order updatedOrder) {
        order.setStatus(updatedOrder.getStatus());
        order.setCustomer(updatedOrder.getCustomer());
        order.setItems(updatedOrder.getItems());
    }

}
