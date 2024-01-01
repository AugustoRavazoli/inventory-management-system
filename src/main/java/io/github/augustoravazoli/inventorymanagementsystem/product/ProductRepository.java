package io.github.augustoravazoli.inventorymanagementsystem.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    List<Product> findAllByNameContainingIgnoreCase(String name);

}
