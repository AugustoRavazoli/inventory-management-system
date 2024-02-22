package io.github.augustoravazoli.inventorymanagementsystem.product;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByIdAndOwner(long id, User owner);

    boolean existsByNameAndOwner(String name, User owner);

    Optional<Product> findByIdAndOwner(long id, User owner);

    Optional<Product> findByNameAndOwner(String name, User owner);

    Page<Product> findAllByOwner(User owner, Pageable pageable);

    List<Product> findAllByOwner(User owner, Sort sort);

    List<Product> findAllByNameContainingIgnoreCaseAndOwner(String name, User owner);

}
