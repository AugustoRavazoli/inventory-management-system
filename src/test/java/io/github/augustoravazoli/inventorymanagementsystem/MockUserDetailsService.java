package io.github.augustoravazoli.inventorymanagementsystem;

import io.github.augustoravazoli.inventorymanagementsystem.user.User;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;

@TestConfiguration
public class MockUserDetailsService {

    @Bean
    public UserDetailsService mockUserDetailsService() {
        return email -> new User("user", "user@email.com", "password");
    }

}
