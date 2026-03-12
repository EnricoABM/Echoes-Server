package com.n0hana.echoes_server.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.n0hana.echoes_server.dto.TwoFactorDto;

@Service
public class EmailNotifier implements TwoFactorNotifier {

    private final JavaMailSender mailSender;

    public EmailNotifier(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void send(TwoFactorDto dto) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(dto.dto().email());
        message.setSubject("Your verification code");
        message.setText("Your login code is: " + dto.code());

        mailSender.send(message);
    }
}
