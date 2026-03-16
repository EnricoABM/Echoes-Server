package com.n0hana.echoes_server.service.notifier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.n0hana.echoes_server.dto.TwoFactorDto;

/**==================================
 *  CLASSE DE ENVIO DE NOTIFICÕES
 * ==================================
 * Implementa comunicação com um 
 * cliente de EMAIL para envio das 
 * notificações aos usuários.
*/
@Service
@Primary
public class EmailNotifier implements TwoFactorNotifier {

    @Value("${api.smtp.token}")
    private String apiKey;

    @Override
    public void send(TwoFactorDto dto) {

        if (apiKey == null) {
            throw new IllegalStateException("MAILGUN API KEY não configurada");
        }

        try {

            HttpResponse<JsonNode> response = Unirest.post(
                "https://api.mailgun.net/v3/sandbox848be28020374b59b903e3911b156564.mailgun.org/messages"
            )
            .basicAuth("api", apiKey)
            .field("from", "Echoes")
            .field("to", dto.email())
            .field("subject", "Echoes Validation Code")
            .field("text", "Código de Validação: " + dto.code())
            .asJson();

            if (response.getStatus() != 200) {
                throw new RuntimeException("Erro Mailgun: " + response.getBody());
            }

        } catch (UnirestException e) {
            throw new RuntimeException("Falha ao enviar email", e);
        }
    }
}
