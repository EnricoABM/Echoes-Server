package com.n0hana.echoes_server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.n0hana.echoes_server.dto.UserInfoResponseDTO;
import com.n0hana.echoes_server.model.User;
import com.n0hana.echoes_server.service.auth.AuthService;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponseDTO> getUserInfo() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return ResponseEntity.ok(new UserInfoResponseDTO(
            user.getName(),
            user.getEmail(),
            user.getRole().toString()
        ));
    }
    
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        authService.deleteAccount(user);
        
        return ResponseEntity.noContent().build();
    }
    
}
