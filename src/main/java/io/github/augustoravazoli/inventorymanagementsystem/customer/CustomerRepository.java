package io.github.augustoravazoli.inventorymanagementsystem.customer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Page<Customer> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

}
