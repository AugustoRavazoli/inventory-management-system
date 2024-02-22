package io.github.augustoravazoli.inventorymanagementsystem.order;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsByCustomerIdAndOwner(long customerId, User owner);

    boolean existsByItemsProductIdAndOwner(long productId, User owner);

    Optional<Order> findByIdAndOwner(long id, User owner);

    Page<Order> findAllByStatusAndOwner(OrderStatus status, User owner, Pageable pageable);

    List<Order> findAllByStatusAndCustomerNameContainingIgnoreCaseAndOwner(OrderStatus status, String customerName, User owner);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items")
    List<Order> findAllWithItems();

}
