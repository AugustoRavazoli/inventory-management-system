package io.github.augustoravazoli.inventorymanagementsystem.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    Page<Product> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

}
