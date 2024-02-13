package io.github.augustoravazoli.inventorymanagementsystem.user;

import io.github.augustoravazoli.inventorymanagementsystem.util.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Map;

@Service
public class UserEmailSender {

    private static final Logger logger = LoggerFactory.getLogger(UserEmailSender.class);

    private final EmailSender emailSender;

    public UserEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public void sendVerificationEmail(User user, String token) {
        var to = user.getEmail();
        var subject = "verification-email.subject";
        var template = "user/verification-email";
        var variables = Map.<String, Object>of("verificationLink", buildVerificationLink(token));
        var locale = LocaleContextHolder.getLocale();
        logger.info("Sending verification email to user {}", user.getEmail());
        emailSender.sendHtmlEmail(to, subject, template, variables, locale);
    }

    private String buildVerificationLink(String token) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("verify-account")
                .queryParam("token", token)
                .build()
                .toUriString();
    }

}
