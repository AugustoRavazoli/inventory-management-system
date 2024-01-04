package io.github.augustoravazoli.inventorymanagementsystem.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    Optional<Product> findByName(String name);

    List<Product> findAllByNameContainingIgnoreCase(String name);

}
