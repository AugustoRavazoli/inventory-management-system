package io.github.augustoravazoli.inventorymanagementsystem.util;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;

import static java.util.Collections.emptyMap;

@Component
public class EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MessageSource messageSource;

    public EmailSender(JavaMailSender mailSender, TemplateEngine templateEngine, MessageSource messageSource) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.messageSource = messageSource;
    }

    @Async
    @Retryable(retryFor = MailException.class)
    public void sendHtmlEmail(String to, String subject, String template, Locale locale) {
        sendHtmlEmail(to, subject, template, emptyMap(), locale);
    }

    @Async
    @Retryable(retryFor = MailException.class)
    public void sendHtmlEmail(String to, String subject, String template, Map<String, Object> variables, Locale locale) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message);
            helper.setTo(to);
            helper.setSubject(messageSource.getMessage(subject, null, locale));
            helper.setText(templateEngine.process(template, contextFromMap(variables, locale)), true);
            logger.info("Sending email to {}", to);
            mailSender.send(message);
        } catch (MailException | MessagingException e) {
            logger.info("Failed to send email to {}", to);
            throw new RuntimeException(e);
        }
    }

    private Context contextFromMap(Map<String, Object> variables, Locale locale) {
        var context = new Context(locale);
        context.setVariables(variables);
        return context;
    }

}
