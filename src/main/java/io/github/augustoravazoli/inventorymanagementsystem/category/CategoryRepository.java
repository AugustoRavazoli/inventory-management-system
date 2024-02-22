package io.github.augustoravazoli.inventorymanagementsystem.category;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByIdAndOwner(long id, User owner);

    boolean existsByNameAndOwner(String name, User owner);

    Optional<Category> findByIdAndOwner(long id, User owner);

    Optional<Category> findByNameAndOwner(String name, User owner);

    Page<Category> findAllByOwner(User owner, Pageable pageable);

    List<Category> findAllByOwner(User owner, Sort sort);

    List<Category> findAllByNameContainingIgnoreCaseAndOwner(String name, User owner);

}
