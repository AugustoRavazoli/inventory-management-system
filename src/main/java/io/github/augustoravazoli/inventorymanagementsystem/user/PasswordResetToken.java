package io.github.augustoravazoli.inventorymanagementsystem.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

@Entity
public class PasswordResetToken {

    @Id
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String token;

    @NotNull
    @Column(nullable = false)
    private Instant expirationTime;

    @NotNull
    @OneToOne(optional = false)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public PasswordResetToken() {}

    public PasswordResetToken(String token, Instant expirationTime, User user) {
        this.token = token;
        this.expirationTime = expirationTime;
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public boolean expired() {
        return Instant.now().isAfter(expirationTime);
    }

    @Override
    public String toString() {
        return token;
    }

}
