package io.github.augustoravazoli.inventorymanagementsystem.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByName(String name);

    Optional<Customer> findByName(String name);

    List<Customer> findAllByNameContainingIgnoreCase(String name);

}
