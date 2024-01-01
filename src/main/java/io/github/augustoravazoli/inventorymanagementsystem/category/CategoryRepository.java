package io.github.augustoravazoli.inventorymanagementsystem.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    List<Category> findAllByNameContainingIgnoreCase(String name);

}
