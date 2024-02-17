package io.github.augustoravazoli.inventorymanagementsystem.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEmailSender userEmailSender;

    public UserService(UserRepository userRepository, VerificationTokenRepository verificationTokenRepository, PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder, UserEmailSender userEmailSender) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
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

    @Transactional
    public void sendPasswordResetEmail(String email) {
        var optionalPasswordResetToken = passwordResetTokenRepository.findByUserEmailAndUserStatus(email, AccountStatus.ACTIVE);
        if (optionalPasswordResetToken.isPresent() && !optionalPasswordResetToken.get().expired()) {
            var passwordResetToken = optionalPasswordResetToken.get();
            logger.info("Reusing existing token");
            userEmailSender.sendPasswordResetEmail(passwordResetToken.getUser(), passwordResetToken.toString());
            return;
        }
        optionalPasswordResetToken.ifPresent(passwordResetTokenRepository::delete);
        var user = userRepository.findByEmailAndStatus(email, AccountStatus.ACTIVE)
                .orElseThrow(NonexistentUserException::new);
        logger.info("Token nonexistent or expired, a new one will be generated");
        userEmailSender.sendPasswordResetEmail(user, generatePasswordResetToken(user));
    }

    public void validatePasswordResetToken(String token) {
        var passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(TokenNotFoundException::new);
        if (passwordResetToken.expired()) {
            logger.info("Token expired, throwing exception");
            throw new TokenExpiredException();
        }
    }

    @Transactional
    public void resetPassword(String newPassword, String token) {
        var passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(TokenNotFoundException::new);
        if (passwordResetToken.expired()) {
            logger.info("Token expired, throwing exception");
            throw new TokenExpiredException();
        }
        var user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.delete(passwordResetToken);
        logger.info("Updating password for user {}", user.getEmail());
    }

    public void updatePassword(String password, String newPassword, User user) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            logger.info("Password don't matches existing password, throwing exception");
            throw new PasswordMismatchException();
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        logger.info("Updating password for user {}", user.getEmail());
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

    private String generatePasswordResetToken(User user) {
        var token = UUID.randomUUID().toString();
        var expirationTime = Instant.now().plus(15, ChronoUnit.MINUTES);
        var passwordResetToken = new PasswordResetToken(token, expirationTime, user);
        logger.info("Generating password reset token for user {}", user.getEmail());
        return passwordResetTokenRepository.save(passwordResetToken).toString();
    }

}
