package io.github.augustoravazoli.inventorymanagementsystem.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByTokenAndUserStatus(String token, AccountStatus status);

    Optional<VerificationToken> findByUserEmailAndUserStatus(String email, AccountStatus status);

}
