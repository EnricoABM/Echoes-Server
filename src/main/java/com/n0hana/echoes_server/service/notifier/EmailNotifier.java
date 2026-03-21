package com.n0hana.echoes_server.service.notifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.n0hana.echoes_server.dto.TwoFactorDto;

import lombok.RequiredArgsConstructor;

/**==================================
 *  CLASSE DE ENVIO DE NOTIFICÕES
 * ==================================
 * Implementa comunicação com um 
 * cliente de EMAIL para envio das 
 * notificações aos usuários.
*/
@Service
@RequiredArgsConstructor
public class EmailNotifier implements TwoFactorNotifier {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String origin;

    @Override
    public void send(TwoFactorDto dto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            
            message.setFrom(origin);
            message.setTo(dto.email());
            message.setSubject("Echoes Validation Code");
            message.setText(dto.code());

            javaMailSender.send(message);
        } catch (Exception e) {
            System.out.println("Erro ao enviar email: " + e.getMessage());
        }
    }
}
