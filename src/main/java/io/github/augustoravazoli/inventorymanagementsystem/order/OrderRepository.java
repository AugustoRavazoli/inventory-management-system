package io.github.augustoravazoli.inventorymanagementsystem.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    long countByStatus(Order.Status status);

    boolean existsByCustomerId(long customerId);

    Page<Order> findAllByStatus(Order.Status status, Pageable pageable);

    List<Order> findAllByStatusAndCustomerNameContainingIgnoreCase(Order.Status status, String customerName);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items")
    List<Order> findAllWithItems();

}
