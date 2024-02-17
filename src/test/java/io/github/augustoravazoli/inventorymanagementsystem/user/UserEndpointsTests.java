package io.github.augustoravazoli.inventorymanagementsystem.user;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import io.github.augustoravazoli.inventorymanagementsystem.TestApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.icegreen.greenmail.configuration.GreenMailConfiguration.aConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Import(TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserEndpointsTests {

    @RegisterExtension
    static GreenMailExtension greenMail = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(aConfig().withUser("myuser", "secret"));

    @Autowired
    private MockMvc client;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;


    @AfterEach
    void tearDown() {
        verificationTokenRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    class RegisterUserTests {

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
            var userOptional = userRepository.findByEmail("user@email.com");
            assertThat(userOptional).get()
                    .matches(user -> passwordEncoder.matches("password", user.getPassword()))
                    .extracting("name", "email", "status")
                    .containsExactly("user", "user@email.com", AccountStatus.UNVERIFIED);
            greenMail.waitForIncomingEmail(1);
            var message = greenMail.getReceivedMessages()[0];
            assertThat(message.getAllRecipients()[0].toString()).isEqualTo("user@email.com");
            assertThat(message.getSubject()).isEqualTo("Verify your account on Inventory Management System");
            assertThat((String) message.getContent()).containsSequence("http://localhost/verify-account?token");
        }

    }

    @Nested
    class VerifyAccountTests {

        @BeforeEach
        void setup() {
            var user = new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO");
            verificationTokenRepository.save(new VerificationToken("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", Instant.now().plus(24, ChronoUnit.HOURS), user));
        }

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
            var optionalUser = userRepository.findByEmail("user@email.com");
            assertThat(optionalUser).get()
                    .hasFieldOrPropertyWithValue("status", AccountStatus.ACTIVE)
                    .hasFieldOrPropertyWithValue("enabled", true);
            assertThat(verificationTokenRepository.count()).isZero();
        }

    }

    @Nested
    class ResendVerificationEmailTests {

        @BeforeEach
        void setup() {
            var user = new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO");
            verificationTokenRepository.save(new VerificationToken("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", Instant.now().plus(24, ChronoUnit.HOURS), user));
        }

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
            greenMail.waitForIncomingEmail(1);
            var message = greenMail.getReceivedMessages()[0];
            assertThat(message.getAllRecipients()[0].toString()).isEqualTo("user@email.com");
            assertThat(message.getSubject()).isEqualTo("Verify your account on Inventory Management System");
            assertThat((String) message.getContent()).containsSequence("http://localhost/verify-account?token");
        }

    }

    @Nested
    class SendPasswordResetEmailTests {

        @BeforeEach
        void setup() {
            var user = new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO");
            user.setStatus(AccountStatus.ACTIVE);
            userRepository.save(user);
        }

        @Test
        void requestPasswordReset() throws Exception {
            // when
            var result = client.perform(post("/request-password-reset")
                    .param("email", "user@email.com")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    view().name("user/request-password-reset-success")
            );
            greenMail.waitForIncomingEmail(1);
            var message = greenMail.getReceivedMessages()[0];
            assertThat(message.getAllRecipients()[0].toString()).isEqualTo("user@email.com");
            assertThat(message.getSubject()).isEqualTo("Reset your password on Inventory Management System");
            assertThat((String) message.getContent()).containsSequence("http://localhost/reset-password?token");
        }

    }

    @Nested
    class ResetPasswordTests {

        @BeforeEach
        void setup() {
            var user = new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO");
            user.setStatus(AccountStatus.ACTIVE);
            passwordResetTokenRepository.save(new PasswordResetToken("xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", Instant.now().plus(15, ChronoUnit.MINUTES), user));
        }

        @Test
        void resetPassword() throws Exception {
            // when
            var result = client.perform(post("/reset-password")
                    .param("new-password", "newPassword")
                    .param("token", "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx")
                    .with(csrf())
            );
            // then
            result.andExpectAll(
                    status().isOk(),
                    view().name("user/password-updated")
            );
            var optionalUser = userRepository.findByEmail("user@email.com");
            assertThat(optionalUser).get()
                    .matches(user -> passwordEncoder.matches("newPassword", user.getPassword()));
            assertThat(passwordResetTokenRepository.count()).isZero();
        }

    }

    @Nested
    class UpdatePasswordTests {

        @BeforeEach
        void setup() {
            userRepository.save(new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO"));
        }

        @Test
        @WithUserDetails(value = "user@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
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
            var optionalUser = userRepository.findByEmail("user@email.com");
            assertThat(optionalUser).get().matches(user -> passwordEncoder.matches("newPassword", user.getPassword()));
        }

    }

    @Nested
    class DeleteUserAccountTests {

        @BeforeEach
        void setup() {
            userRepository.save(new User("user", "user@email.com", "$2a$10$gYCEDfFbidA3IInCfzcXdugclrYR/6FbQuogN7Ixc3ohWi90MEXiO"));
        }

        @Test
        @WithUserDetails(value = "user@email.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
        void deleteUserAccount() throws Exception {
            // when
            var result = client.perform(post("/delete-account").with(csrf()));
            // then
            result.andExpectAll(
                    status().isOk(),
                    forwardedUrl("/logout")
            );
            var optionalUser = userRepository.findByEmail("user@email.com");
            assertThat(optionalUser).get()
                    .hasFieldOrPropertyWithValue("status", AccountStatus.DELETED)
                    .hasFieldOrPropertyWithValue("enabled", false);
        }

    }

}
