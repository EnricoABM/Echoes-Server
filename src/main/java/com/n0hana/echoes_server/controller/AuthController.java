package com.n0hana.echoes_server.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.n0hana.echoes_server.dto.AuthRequestDTO;
import com.n0hana.echoes_server.dto.AuthResponseDTO;
import com.n0hana.echoes_server.dto.RegisterRequestDTO;
import com.n0hana.echoes_server.model.User;
import com.n0hana.echoes_server.repository.UserRepository;
import com.n0hana.echoes_server.service.TokenService;

import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO dto) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        String jwtToken = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new AuthResponseDTO(jwtToken));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO dto) {
        if (userRepository.findUserByEmail(dto.email()) != null) return ResponseEntity.badRequest().build();
        
        String encryptedPassword = passwordEncoder.encode(dto.password());
        User user = new User(dto.name(), dto.email(), encryptedPassword, dto.role());
    
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }
    
    
}
