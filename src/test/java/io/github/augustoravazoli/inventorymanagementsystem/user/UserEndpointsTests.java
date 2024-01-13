package io.github.augustoravazoli.inventorymanagementsystem.user;

import io.github.augustoravazoli.inventorymanagementsystem.TestApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@Import(TestApplication.class)
@ActiveProfiles("test")
@AutoConfigureMockMvc
class UserEndpointsTests {

    @Autowired
    private MockMvc client;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @AfterEach
    void tearDown() {
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
                    view().name("user/success")
            );
            var userOptional = userRepository.findByEmail("user@email.com");
            assertThat(userOptional).get()
                    .matches(user -> passwordEncoder.matches("password", user.getPassword()))
                    .extracting("name", "email")
                    .containsExactly("user", "user@email.com");
        }

    }

}
