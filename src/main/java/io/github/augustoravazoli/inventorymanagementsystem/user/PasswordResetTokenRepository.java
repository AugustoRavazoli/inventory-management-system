package io.github.augustoravazoli.inventorymanagementsystem.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByUserEmailAndUserStatus(String email, AccountStatus status);

    @Procedure("delete_expired_password_reset_tokens")
    void deleteExpired();

}
