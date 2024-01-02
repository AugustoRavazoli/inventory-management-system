package io.github.augustoravazoli.inventorymanagementsystem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.logout;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Void.class)
@Import(SecurityConfiguration.class)
@ActiveProfiles("test")
class SecurityTests {

    @Autowired
    private MockMvc client;

    @Test
    void retrieveLoginPage() throws Exception {
        // when
        var result = client.perform(get("/login"));
        // then
        result.andExpectAll(
                status().isOk(),
                view().name("login")
        );
    }

    @Test
    void loginUser() throws Exception {
        // when
        var result = client.perform(formLogin()
                .user("admin")
                .password("admin")
        );
        // then
        result.andExpectAll(
                status().isFound(),
                redirectedUrl("/dashboard")
        );
    }

    @Test
    void doNotLoginUserWithInvalidUsernameOrPassword() throws Exception {
        // when
        var result = client.perform(formLogin()
                .user("invalid")
                .password("invalid")
        );
        // then
        result.andExpectAll(
                status().isFound(),
                redirectedUrl("/login?error")
        );
    }

    @Test
    void logoutUser() throws Exception {
        // when
        var result = client.perform(logout());
        // then
        result.andExpectAll(
                status().isFound(),
                redirectedUrl("/login?logout")
        );
    }

    @Test
    void redirectUnauthenticatedUserToLoginPage() throws Exception {
        // when
        var result = client.perform(get("/protected-url"));
        // then
        result.andExpectAll(
                status().isFound(),
                redirectedUrl("http://localhost/login")
        );
    }

    @Test
    @WithMockUser
    void allowAuthenticatedUserToAccessProtectedPages() throws Exception {
        // when
        var result = client.perform(get("/protected-url"));
        // then
        result.andExpectAll(status().isNotFound());
    }

}
