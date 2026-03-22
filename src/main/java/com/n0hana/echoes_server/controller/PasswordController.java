package com.n0hana.echoes_server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.n0hana.echoes_server.dto.PasswordDTO;
import com.n0hana.echoes_server.service.password.PasswordResetService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordResetService service;

    @PostMapping("/forgot")
    public ResponseEntity<PasswordDTO.ForgotResponse> forgotPassword(@RequestBody PasswordDTO.ForgotRequest dto) {
        
        service.requestReset(dto);
        return ResponseEntity.ok(
            new PasswordDTO.ForgotResponse("Caso o e-mail esteja cadastrado, você receberá o código em brave...")
        ); 
    }

    @PostMapping("/reset")
    public ResponseEntity<PasswordDTO.ResetResponse> resetPassword(@RequestBody PasswordDTO.ResetRequest dto) {
           
        try {
            service.resetPassword(dto);
            return ResponseEntity.ok(new PasswordDTO.ResetResponse("Senha altereado com sucesso."));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new PasswordDTO.ResetResponse("Não foi possível alterar a senha.\n " + e.getMessage()));
        }
    }
    
    @PostMapping("/validate")
    public ResponseEntity<PasswordDTO.ValidateResponse> validatePasswordToChange(@RequestBody PasswordDTO.ValidateRequest dto, @RequestHeader("Authorization") String token) {
        
        String changeToken = service.validatePassword(dto, token);
        if (changeToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(
            new PasswordDTO.ValidateResponse(changeToken)    
        );
    }


    @PostMapping("/change")
    public ResponseEntity<Void> changePassword(@RequestBody PasswordDTO.ChangeRequest dto) {
        try {
            service.changePassowrd(dto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
        
    }
    
    
}
