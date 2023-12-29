package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.customer.CustomerRepository;
import io.github.augustoravazoli.inventorymanagementsystem.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        var productIds = order.getItems().stream().map(item -> item.getProduct().getId()).toList();
        var products = productRepository.findAllById(productIds);
        for (var item : order.getItems()) {
            var product = products.stream()
                    .filter(p -> p.getId().equals(item.getProduct().getId()))
                    .findFirst()
                    .orElse(null);
            if (product == null) {
                throw new InvalidProductException();
            }
            if (item.getQuantity() > product.getQuantity()) {
                throw new ProductWithInsufficientStockException();
            }
            product.setQuantity(product.getQuantity() - item.getQuantity());
        }
        orderRepository.save(order);
    }

    public Page<Order> listOrders(String customerName, Pageable pageable) {
        if (customerName.isEmpty()) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findAllByCustomerNameContainingIgnoreCase(customerName, pageable);
    }

}
