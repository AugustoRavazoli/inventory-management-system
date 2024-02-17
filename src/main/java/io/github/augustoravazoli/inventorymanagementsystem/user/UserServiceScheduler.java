package io.github.augustoravazoli.inventorymanagementsystem.user;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class UserServiceScheduler {

    private final UserRepository userRepository;

    public UserServiceScheduler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "@daily")
    private void deleteUnverifiedUsersWithExpiredTokens() {
        userRepository.deleteUnverifiedUsersWithExpiredTokens();
    }

}
