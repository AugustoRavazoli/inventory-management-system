package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.customer.Customer;
import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CustomerRepository customerRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public void createOrder(Order order) {
        checkCustomer(order.getCustomer());
        checkDuplicates(order.getItems());
        checkProductAvailability(order.getItems());
        updateProductQuantities(order.getItems(), "decrease");
        orderRepository.save(order);
    }

    public Page<Order> listOrders(Order.Status status, int page) {
        return orderRepository.findAllByStatus(status, PageRequest.of(page - 1, 8, Sort.by("date")));
    }

    public List<Order> findOrders(Order.Status status, String customerName) {
        return orderRepository.findAllByStatusAndCustomerNameContainingIgnoreCase(status, customerName);
    }

    public Order findOrder(long id) {
        return orderRepository.findById(id)
                .orElseThrow(OrderNotFoundException::new);
    }

    @Transactional
    public void updateOrder(long id, Order updatedOrder) {
        var order = orderRepository.findById(id).orElseThrow(OrderNotFoundException::new);
        checkCustomer(updatedOrder.getCustomer());
        checkDuplicates(updatedOrder.getItems());
        checkProductAvailability(updatedOrder.getItems());
        updateProductQuantitiesForExistingItems(order.getItems(), updatedOrder.getItems());
        decreaseProductQuantitiesForNewItems(order.getItems(), updatedOrder.getItems());
        resetProductQuantitiesForRemovedItems(order.getItems(), updatedOrder.getItems());
        updateOrderDetails(order, updatedOrder);
        orderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(long id) {
        var order = orderRepository.findById(id).orElseThrow(OrderNotFoundException::new);
        if (order.getStatus() == Order.Status.UNPAID) {
            updateProductQuantities(order.getItems(), "increase");
        }
        orderRepository.delete(order);
    }

    private void checkCustomer(Customer customer) {
        if (!customerRepository.existsById(customer.getId())) {
            throw new InvalidCustomerException();
        }
    }

    private void checkDuplicates(List<OrderItem> items) {
        var elements = new HashSet<Long>();
        if (items.stream().anyMatch(item -> !elements.add(item.getProduct().getId()))) {
            throw new DuplicatedOrderItemException();
        }
    }

    private void checkProductAvailability(List<OrderItem> items) {
        for (var item : items) {
            var product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(InvalidProductException::new);
            if (item.getQuantity() > product.getQuantity()) {
                throw new ProductWithInsufficientStockException();
            }
        }
    }

    private void updateProductQuantities(List<OrderItem> items, String operation) {
        for (var item : items) {
            var product = productRepository.findById(item.getProduct().getId()).orElseThrow();
            switch (operation) {
                case "increase" -> product.increaseQuantity(item.getQuantity());
                case "decrease" -> product.decreaseQuantity(item.getQuantity());
                default -> throw new IllegalArgumentException("No case found for operation " + operation);
            }
        }
    }

    private void updateProductQuantitiesForExistingItems(List<OrderItem> items, List<OrderItem> updatedItems) {
        var existingItems = updatedItems.stream().filter(item -> contains(item, items)).toList();
        var oldItems = items.stream().filter(item -> contains(item, existingItems)).toList();
        updateProductQuantities(oldItems, "increase");
        updateProductQuantities(existingItems, "decrease");
    }

    private void decreaseProductQuantitiesForNewItems(List<OrderItem> items, List<OrderItem> updatedItems) {
        var newItems = updatedItems.stream().filter(item -> !contains(item, items)).toList();
        updateProductQuantities(newItems, "decrease");
    }

    private void resetProductQuantitiesForRemovedItems(List<OrderItem> items, List<OrderItem> updatedItems) {
        var removedItems = items.stream().filter(item -> !contains(item, updatedItems)).toList();
        updateProductQuantities(removedItems, "increase");
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
