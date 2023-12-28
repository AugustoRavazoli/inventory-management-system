package io.github.augustoravazoli.inventorymanagementsystem.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    Page<Category> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

}
