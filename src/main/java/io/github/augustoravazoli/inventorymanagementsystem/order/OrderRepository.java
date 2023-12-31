package io.github.augustoravazoli.inventorymanagementsystem.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByStatus(Order.Status status);

    Page<Order> findAllByStatus(Order.Status status, Pageable pageable);

    Page<Order> findAllByStatusAndCustomerNameContainingIgnoreCase(Order.Status status, String customerName, Pageable pageable);

}
