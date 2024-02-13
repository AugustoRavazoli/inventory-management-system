package io.github.augustoravazoli.inventorymanagementsystem.user;

import io.github.augustoravazoli.inventorymanagementsystem.MockUserDetailsService;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.MailSender;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(MockUserDetailsService.class)
@WithUserDetails
class UserControllerTest {

    @MockBean
    private UserService userService;

    @MockBean
    private MailSender mailSender;

    @Autowired
    private MockMvc client;

    @Nested
    @RecordApplicationEvents
    class RegisterUserTests {

        @Autowired
        private ApplicationEvents applicationEvents;

        @Test
        void retrieveRegisterUserPage() throws Exception {
            // when
            var result = client.perform(get("/register"));
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("user", is(user())),
                    view().name("user/register")
            );
        }

        @Test
        void registerUser() throws Exception {
            // when
            var result = client.perform(post("/register")
                    .param("name", "user")
                    .param("email", "user@email.com")
                    .param("password", "password")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("email", "user@email.com"),
                    view().name("user/verify-account")
            );
            verify(userService, times(1)).registerUser(any(User.class));
        }

        @Test
        void doNotRegisterUserWithEmailTaken() throws Exception {
            // given
            doThrow(UserEmailTakenException.class).when(userService).registerUser(any(User.class));
            // when
            var result = client.perform(post("/register")
                    .param("name", "user")
                    .param("email", "user@email.com")
                    .param("password", "password")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("duplicatedEmail", true),
                    model().attribute("user", is(
                            user("user", "user@email.com", "password"))
                    ),
                    view().name("user/register")
            );
            verify(userService, times(1)).registerUser(any(User.class));
        }

        @Test
        void doNotRegisterUserWithBlankFields() throws Exception {
            // when
            var result = client.perform(post("/register")
                    .param("name", "")
                    .param("email", "")
                    .param("password", "")
                    .with(csrf())
            );
            // then
            result.andExpect(status().isBadRequest());
        }

    }

    @Nested
    class VerifyAccountTests {

        @Test
        void verifyAccount() throws Exception {
            // when
            var result = client.perform(get("/verify-account")
                    .param("token", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    view().name("user/account-verified")
            );
            verify(userService, times(1)).verifyAccount(anyString());
        }

        @Test
        void doNotVerifyAccountWithExpiredToken() throws Exception {
            // given
            doThrow(TokenExpiredException.class).when(userService).verifyAccount(anyString());
            // when
            var result = client.perform(get("/verify-account")
                    .param("token", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    view().name("user/account-expired")
            );
            verify(userService, times(1)).verifyAccount(anyString());
        }

    }

    @Nested
    class ResendVerificationEmailTests {

        @Test
        void resendVerificationEmail() throws Exception {
            // when
            var result = client.perform(post("/resend-verification-email")
                    .param("email", "user@email.com")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    model().attribute("email", "user@email.com"),
                    view().name("user/verify-account")
            );
            verify(userService, times(1)).resendVerificationEmail(anyString());
        }

    }

    @Nested
    class UpdatePasswordTests {

        @Test
        void updatePassword() throws Exception {
            // when
            var result = client.perform(post("/update-password")
                    .param("password", "password")
                    .param("new-password", "newPassword")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/settings?update-password-success")
            );
            verify(userService, times(1)).updatePassword(anyString(), anyString(), any(User.class));
        }

        @Test
        void doNotUpdatePasswordIfPasswordDoNotMatchesExistingPassword() throws Exception {
            // given
            doThrow(PasswordMismatchException.class).when(userService).updatePassword(anyString(), anyString(), any(User.class));
            // when
            var result = client.perform(post("/update-password")
                    .param("password", "wrong")
                    .param("new-password", "newPassword")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isFound(),
                    redirectedUrl("/settings?update-password-error")
            );
            verify(userService, times(1)).updatePassword(anyString(), anyString(), any(User.class));
        }

        @Test
        void doNotUpdatePasswordIfPasswordsAreEmpty() throws Exception {
            // when
            var result = client.perform(post("/update-password")
                    .param("password", "")
                    .param("new-password", "")
                    .with(csrf())
            );
            // then
            result.andExpect(status().isBadRequest());
        }

    }

    @Nested
    class DisableUserTests {

        @Test
        void disableUser() throws Exception {
            // when
            var result = client.perform(post("/delete-account").with(csrf()));
            // then
            result.andExpectAll(
                    status().isOk(),
                    forwardedUrl("/logout")
            );
            verify(userService, times(1)).disableUser(any(User.class));
        }

    }

    private Matcher<User> user(String name, String email, String password) {
        return allOf(
                hasProperty("name", is(name)),
                hasProperty("email", is(email)),
                hasProperty("password", is(password))
        );
    }

    private Matcher<User> user() {
        return allOf(
                hasProperty("name", nullValue()),
                hasProperty("email", nullValue()),
                hasProperty("password", nullValue())
        );
    }

}
