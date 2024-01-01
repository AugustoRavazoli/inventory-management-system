package io.github.augustoravazoli.inventorymanagementsystem.customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByName(String name);

    List<Customer> findAllByNameContainingIgnoreCase(String name);

}
