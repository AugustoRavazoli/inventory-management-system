package io.github.augustoravazoli.inventorymanagementsystem.user;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Nested
    class RegisterUserTests {

        @Test
        void registerUser() {
            // given
            var user = new User("user", "user@email.com", "password");
            when(userRepository.existsByEmail("user@email.com")).thenReturn(false);
            when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
            // when
            userService.registerUser(user);
            // then
            assertThat(user.getPassword()).isEqualTo("encodedPassword");
            verify(passwordEncoder, times(1)).encode("password");
            verify(userRepository, times(1)).save(user);
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
            assertThat(user.isEnabled()).isFalse();
            verify(userRepository, times(1)).save(user);
        }

    }

}
