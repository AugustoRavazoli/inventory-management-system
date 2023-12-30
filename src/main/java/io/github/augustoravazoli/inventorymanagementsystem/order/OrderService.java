package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        if (!customerRepository.existsById(order.getCustomer().getId())) {
            throw new InvalidCustomerException();
        }
        checkDuplicates(order.getItems());
        checkProductAvailability(order.getItems());
        order.getItems().forEach(item -> productRepository.findById(item.getProduct().getId())
                .orElseThrow()
                .decreaseQuantity(item.getQuantity()));
        orderRepository.save(order);
    }

    public Page<Order> listOrders(String customerName, Pageable pageable) {
        if (customerName.isEmpty()) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findAllByCustomerNameContainingIgnoreCase(customerName, pageable);
    }

    public Order findOrder(long id) {
        return orderRepository.findById(id)
                .orElseThrow(OrderNotFoundException::new);
    }

    @Transactional
    public void updateOrder(long id, Order updatedOrder) {
        var order = orderRepository.findById(id).orElseThrow(OrderNotFoundException::new);
        if (!customerRepository.existsById(order.getCustomer().getId())) {
            throw new InvalidCustomerException();
        }
        updateProductQuantitiesForExistingItems(order.getItems(), updatedOrder.getItems());
        decreaseProductQuantitiesForNewItems(order.getItems(), updatedOrder.getItems());
        resetProductQuantitiesForRemovedItems(order.getItems(), updatedOrder.getItems());
        order.setStatus(updatedOrder.getStatus());
        order.setCustomer(updatedOrder.getCustomer());
        order.setItems(updatedOrder.getItems());
        orderRepository.save(order);
    }

    void updateProductQuantitiesForExistingItems(List<OrderItem> items, List<OrderItem> updatedItems) {
        items.forEach(item -> {
            var updatedItem = updatedItems.stream()
                    .filter(ui -> ui.getProduct().getId().equals(item.getProduct().getId()))
                    .findFirst()
                    .orElse(null);
            if (updatedItem == null) {
                return;
            }
            var product = productRepository.findById(updatedItem.getProduct().getId()).orElseThrow();
            var difference = Math.abs(updatedItem.getQuantity() - item.getQuantity());
            if (updatedItem.getQuantity() > item.getQuantity()) {
                product.decreaseQuantity(difference);
            } else if (updatedItem.getQuantity() < item.getQuantity()) {
                product.increaseQuantity(difference);
            }
        });
    }

    void decreaseProductQuantitiesForNewItems(List<OrderItem> items, List<OrderItem> updatedItems) {
        checkDuplicates(updatedItems);
        checkProductAvailability(updatedItems);
        updatedItems.stream()
                .filter(updatedItem -> items.stream()
                        .noneMatch(item -> item.getProduct().getId().equals(updatedItem.getProduct().getId())))
                .forEach(updatedItem -> productRepository.findById(updatedItem.getProduct().getId())
                        .orElseThrow()
                        .decreaseQuantity(updatedItem.getQuantity()));
    }

    void resetProductQuantitiesForRemovedItems(List<OrderItem> items, List<OrderItem> updatedItems) {
        items.stream()
                .filter(item -> updatedItems.stream()
                        .noneMatch(updatedItem -> updatedItem.getProduct().getId().equals(item.getProduct().getId())))
                .forEach(item -> productRepository.findById(item.getProduct().getId())
                        .orElseThrow()
                        .increaseQuantity(item.getQuantity()));
    }

    private void checkDuplicates(List<OrderItem> items) {
        var elements = new HashSet<Long>();
        if (items.stream().anyMatch(item -> !elements.add(item.getProduct().getId()))) {
            throw new DuplicatedOrderItemException();
        }
    }

    private void checkProductAvailability(List<OrderItem> items) {
        var productIds = items.stream().map(item -> item.getProduct().getId()).toList();
        var products = productRepository.findAllById(productIds);
        items.forEach(item -> {
            var product = products.stream()
                    .filter(p -> p.getId().equals(item.getProduct().getId()))
                    .findFirst()
                    .orElseThrow(InvalidProductException::new);
            if (item.getQuantity() > product.getQuantity()) {
                throw new ProductWithInsufficientStockException();
            }
        });
    }

}
