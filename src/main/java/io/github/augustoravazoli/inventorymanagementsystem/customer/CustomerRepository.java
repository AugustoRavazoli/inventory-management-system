package io.github.augustoravazoli.inventorymanagementsystem.customer;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    long countByOwner(User owner);

    boolean existsByIdAndOwner(long id, User owner);

    boolean existsByNameAndOwner(String name, User owner);

    Optional<Customer> findByIdAndOwner(long id, User owner);

    Optional<Customer> findByNameAndOwner(String name, User owner);

    Page<Customer> findAllByOwner(User owner, Pageable pageable);

    List<Customer> findAllByOwner(User owner, Sort sort);

    List<Customer> findAllByNameContainingIgnoreCaseAndOwner(String name, User owner);

}
