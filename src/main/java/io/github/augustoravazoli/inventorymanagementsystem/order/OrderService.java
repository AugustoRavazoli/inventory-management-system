package io.github.augustoravazoli.inventorymanagementsystem.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }


    public Page<Order> listOrders(String customerName, Pageable pageable) {
        if (customerName.isEmpty()) {
            return orderRepository.findAll(pageable);
        }
        return orderRepository.findAllByCustomerNameContainingIgnoreCase(customerName, pageable);
    }

}
