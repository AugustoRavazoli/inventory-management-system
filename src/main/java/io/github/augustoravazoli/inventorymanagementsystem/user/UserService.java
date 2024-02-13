package io.github.augustoravazoli.inventorymanagementsystem.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEmailSender userEmailSender;

    public UserService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository, PasswordEncoder passwordEncoder, UserEmailSender userEmailSender) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEmailSender = userEmailSender;
    }

    public void registerUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            logger.info("User with email taken, throwing exception");
            throw new UserEmailTakenException();
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        logger.info("Registering user {}", user.getEmail());
        userEmailSender.sendVerificationEmail(user, generateVerificationToken(user));
    }

    public void verifyAccount(String token) {
        var verificationToken = verificationTokenRepository.findByTokenAndUserStatus(token, AccountStatus.UNVERIFIED)
                .orElseThrow(TokenNotFoundException::new);
        if (verificationToken.expired()) {
            logger.info("Token expired, throwing exception");
            throw new TokenExpiredException();
        }
        var user = verificationToken.getUser();
        user.setStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        logger.info("Activating account for user {}", user.getEmail());
    }

    public void resendVerificationEmail(String email) {
        var verificationToken = verificationTokenRepository.findByUserEmailAndUserStatus(email, AccountStatus.UNVERIFIED)
                .orElseThrow(TokenNotFoundException::new);
        var user = verificationToken.getUser();
        if (verificationToken.expired()) {
            logger.info("Token expired, throwing exception");
            throw new TokenNotFoundException();
        }
        userEmailSender.sendVerificationEmail(user, verificationToken.toString());
    }

    public void updatePassword(String password, String newPassword, User user) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.info("Password don't matches existing password, throwing exception");
            throw new PasswordMismatchException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Updating password for user {}", user.getPassword());
    }

    public void disableUser(User user) {
        user.setStatus(AccountStatus.DELETED);
        userRepository.save(user);
        logger.info("Disabling account for user {}", user.getEmail());
    }

    private String generateVerificationToken(User user) {
        var token = UUID.randomUUID().toString();
        var expirationTime = Instant.now().plus(24, ChronoUnit.HOURS);
        var verificationToken = new VerificationToken(token, expirationTime, user);
        logger.info("Generating verification token for user {}", user.getEmail());
        return verificationTokenRepository.save(verificationToken).toString();
    }

    @Scheduled(cron = "@daily")
    private void deleteUnverifiedUsersWithExpiredTokens() {
        userRepository.deleteUnverifiedUsersWithExpiredTokens();
    }

}
