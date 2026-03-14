package com.n0hana.echoes_server.service.notifier;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.n0hana.echoes_server.dto.TwoFactorDto;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.MailtrapMail;
import io.mailtrap.model.request.emails.Address;

@Primary
@Service
public class EmailNotifier implements TwoFactorNotifier {

    @Value("${MAILTRAP_TOKEN}")
    private String TOKEN;
    @Value("${MAILTRAP_EMAIL}")
    private String EMAIL;

    @Override
    public void send(TwoFactorDto dto) {
        final MailtrapConfig config = new MailtrapConfig.Builder()

            .token(TOKEN)

            .build();


        final MailtrapClient client = MailtrapClientFactory.createMailtrapClient(config);
        final MailtrapMail mail = MailtrapMail.builder()
            .from(new Address("hello@demomailtrap.co", "Mailtrap Test"))
            .to(List.of(new Address(EMAIL)))
            .subject("Seu código de verificação")
            .text("Seu login é: " + dto.code())
            .build();

        try {
            System.out.println(client.send(mail));
        } catch(Exception e) {
          System.out.println("Caught e: " + e);
        }
    }
}
