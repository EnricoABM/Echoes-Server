package com.n0hana.echoes_server.service.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    
    @Value("${spring.mail.username}")
    private String origin;

    private final JavaMailSender javaMailSender;

    public void sendPasswordResetToken(String email, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            message.setFrom(origin);
            message.setTo(email);
            message.setSubject("Echoes Validation Code");
            message.setText(code);

            javaMailSender.send(message);
        } catch (Exception e) {
            System.out.println("Erro ao enviar email: " + e.getMessage());
        }
    }

    private void sendTwoFactorToken(String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            message.setFrom(origin);
            message.setTo(email);
            message.setSubject("Echoes Validation Code");
            message.setText(token);

            javaMailSender.send(message);
        } catch (Exception e) {
            System.out.println("Erro ao enviar email: " + e.getMessage());
        }
    }
}
