package io.github.augustoravazoli.inventorymanagementsystem.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByCustomerNameContainingIgnoreCase(String customerName, Pageable pageable);

}
