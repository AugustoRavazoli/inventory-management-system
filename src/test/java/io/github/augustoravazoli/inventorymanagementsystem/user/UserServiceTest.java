package io.github.augustoravazoli.inventorymanagementsystem.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String TOKEN = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEmailSender userEmailSender;

    @Nested
    class RegisterUserTests {

        @Test
        void registerUser() {
            // given
            var user = new User("user", "user@email.com", "password");
            var verificationToken = new VerificationToken(TOKEN, Instant.now().plus(24, ChronoUnit.HOURS), user);
            when(userRepository.existsByEmail("user@email.com")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            when(verificationTokenRepository.save(any(VerificationToken.class))).thenReturn(verificationToken);
            // when
            userService.registerUser(user);
            // then
            assertThat(user.getStatus()).isEqualTo(AccountStatus.UNVERIFIED);
            assertThat(user.getPassword()).isEqualTo("encodedPassword");
            verify(passwordEncoder, times(1)).encode("password");
            verify(userRepository, times(1)).save(user);
            verify(userEmailSender, times(1)).sendVerificationEmail(user, TOKEN);
        }

        @Test
        void doNotRegisterUserWithEmailTaken() {
            // given
            var user = new User("user", "user@email.com", "password");
            when(userRepository.existsByEmail("user@email.com")).thenReturn(true);
            // when
            var exception = assertThatThrownBy(() -> userService.registerUser(user));
            // then
            exception.isInstanceOf(UserEmailTakenException.class);
            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
            verify(userEmailSender, never()).sendVerificationEmail(any(User.class), anyString());
        }

    }

    @Nested
    class VerifyAccountTests {

        @Test
        void verifyAccount() {
            // given
            var user = new User("user", "user@email.com", "password");
            var verificationToken = new VerificationToken(TOKEN, Instant.now().plus(24, ChronoUnit.HOURS), user);
            when(verificationTokenRepository.findByTokenAndUserStatus(TOKEN, AccountStatus.UNVERIFIED)).thenReturn(Optional.of(verificationToken));
            // when
            userService.verifyAccount(TOKEN);
            // then
            assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
            assertThat(user.isEnabled()).isTrue();
            verify(userRepository, times(1)).save(user);
        }

        @Test
        void doNotVerifyAccountWithExpiredToken() {
            // given
            var user = new User("user", "user@email.com", "password");
            var verificationToken = new VerificationToken(TOKEN, Instant.now().minus(24, ChronoUnit.HOURS), user);
            when(verificationTokenRepository.findByTokenAndUserStatus(TOKEN, AccountStatus.UNVERIFIED)).thenReturn(Optional.of(verificationToken));
            // when
            var exception = assertThatThrownBy(() -> userService.verifyAccount(TOKEN));
            // then
            exception.isInstanceOf(TokenExpiredException.class);
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        void doNotVerifyAccountWithNonexistentToken() {
            // given
            when(verificationTokenRepository.findByTokenAndUserStatus(TOKEN, AccountStatus.UNVERIFIED)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> userService.verifyAccount(TOKEN));
            // then
            exception.isInstanceOf(TokenNotFoundException.class);
            verify(userRepository, never()).save(any(User.class));
        }

    }

    @Nested
    class ResendVerificationEmailTests {

        @Test
        void resendVerificationEmail() {
            // given
            var user = new User("user", "user@email.com", "password");
            var verificationToken = new VerificationToken(TOKEN, Instant.now().plus(24, ChronoUnit.HOURS), user);
            when(verificationTokenRepository.findByUserEmailAndUserStatus("user@email.com", AccountStatus.UNVERIFIED)).thenReturn(Optional.of(verificationToken));
            // when
            userService.resendVerificationEmail("user@email.com");
            // then
            verify(userEmailSender, times(1)).sendVerificationEmail(user, TOKEN);
        }

        @Test
        void doNotResendVerificationEmailIfTokenDoesNotExists() {
            // given
            when(verificationTokenRepository.findByUserEmailAndUserStatus("user@email.com", AccountStatus.UNVERIFIED)).thenReturn(Optional.empty());
            // when
            var exception = assertThatThrownBy(() -> userService.resendVerificationEmail("user@email.com"));
            // then
            exception.isInstanceOf(TokenNotFoundException.class);
            verify(userEmailSender, never()).sendVerificationEmail(any(User.class), anyString());
        }

        @Test
        void doNotResendVerificationEmailIfTokenExpired() {
            // given
            var user = new User("user", "user@email.com", "password");
            var verificationToken = new VerificationToken(TOKEN, Instant.now().minus(24, ChronoUnit.HOURS), user);
            when(verificationTokenRepository.findByUserEmailAndUserStatus("user@email.com", AccountStatus.UNVERIFIED)).thenReturn(Optional.of(verificationToken));
            // when
            var exception = assertThatThrownBy(() -> userService.resendVerificationEmail("user@email.com"));
            // then
            exception.isInstanceOf(TokenNotFoundException.class);
            verify(userEmailSender, never()).sendVerificationEmail(any(User.class), anyString());
        }

    }

    @Nested
    class UpdatePasswordTests {

        @Test
        void updatePassword() {
            // given
            var user = new User("user", "user@email.com", "encodedPassword");
            when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
            // when
            userService.updatePassword("password", "newPassword", user);
            // then
            assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
            verify(userRepository, times(1)).save(user);
        }

        @Test
        void doNotUpdatePasswordIfPasswordDoNotMatchesExistingPassword() {
            // given
            var user = new User("user", "user@email.com", "encodedPassword");
            when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);
            // when
            var exception = assertThatThrownBy(() -> userService.updatePassword("wrong", "newPassword", user));
            // then
            exception.isInstanceOf(PasswordMismatchException.class);
            verify(userRepository, never()).save(any(User.class));
        }

    }

    @Nested
    class DisableUserTests {

        @Test
        void disableUser() {
            // given
            var user = new User("user", "user@email.com", "password");
            // when
            userService.disableUser(user);
            // then
            assertThat(user.getStatus()).isEqualTo(AccountStatus.DELETED);
            assertThat(user.isEnabled()).isFalse();
            verify(userRepository, times(1)).save(user);
        }

    }

}
