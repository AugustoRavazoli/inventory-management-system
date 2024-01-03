package io.github.augustoravazoli.inventorymanagementsystem.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    Optional<Category> findByName(String name);

    List<Category> findAllByNameContainingIgnoreCase(String name);

}
