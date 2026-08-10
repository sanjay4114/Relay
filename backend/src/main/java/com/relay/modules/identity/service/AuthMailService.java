package com.relay.modules.identity.service;

import com.relay.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthMailService {

    private final JavaMailSender mailSender;
    private final AuthProperties authProperties;

    public void sendPasswordResetEmail(String email, String rawToken) {
        String resetUrl = authProperties.frontendUrl() + "/reset-password?token=" + rawToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset your Relay password");
        message.setText("""
                You requested a password reset for your Relay account.

                Click the link below to set a new password (valid for 1 hour):
                %s

                If you did not request this, you can safely ignore this email.
                """.formatted(resetUrl));

        try {
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send password reset email to {}", email, ex);
        }
    }
}
