package io.github.augustoravazoli.inventorymanagementsystem.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.info("User with email taken, throwing exception");
            throw new UserEmailTakenException();
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        logger.info("Registering user {}", user.getEmail());
    }

    public void disableUser(User user) {
        user.setEnabled(false);
        userRepository.save(user);
        logger.info("Disabling account for user {}", user.getEmail());
    }

}
