package io.github.augustoravazoli.inventorymanagementsystem.user;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class UserServiceScheduler {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public UserServiceScheduler(UserRepository userRepository, PasswordResetTokenRepository passwordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
    }

    @Scheduled(cron = "@daily")
    private void deleteUnverifiedUsersWithExpiredTokens() {
        userRepository.deleteUnverifiedUsersWithExpiredTokens();
    }

    @Scheduled(cron = "@daily")
    private void deleteExpiredPasswordResetTokens() {
        passwordResetTokenRepository.deleteExpired();
    }

}
