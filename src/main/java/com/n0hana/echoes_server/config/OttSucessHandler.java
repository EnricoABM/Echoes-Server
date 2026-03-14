package com.n0hana.echoes_server.config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.web.authentication.ott.OneTimeTokenGenerationSuccessHandler;
import org.springframework.security.web.authentication.ott.RedirectOneTimeTokenGenerationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OttSucessHandler implements OneTimeTokenGenerationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OttSucessHandler.class);

    private final OneTimeTokenGenerationSuccessHandler redirectHandler = new RedirectOneTimeTokenGenerationSuccessHandler("/ott/sent");

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, OneTimeToken oneTimeToken)
            throws IOException, ServletException {
        
        String magicLink = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/login/ott")
            .queryParam("token", oneTimeToken.getTokenValue())
            .toUriString();

        System.out.println("Magic Link: " + magicLink);
        this.redirectHandler.handle(request, response, oneTimeToken);
    }

    
}
