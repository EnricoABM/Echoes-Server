package com.n0hana.echoes_server.controller;

import java.time.Instant;

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
import com.n0hana.echoes_server.dto.TwoFactorDto;
import com.n0hana.echoes_server.dto.VerifyDTO;
import com.n0hana.echoes_server.model.User;
import com.n0hana.echoes_server.repository.InMemoryTwoFactorRepository;
import com.n0hana.echoes_server.repository.UserRepository;
import com.n0hana.echoes_server.service.TokenService;
import com.n0hana.echoes_server.service.TwoFactorNotifier;
import com.n0hana.echoes_server.service.TwoFactorService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final InMemoryTwoFactorRepository twoFactorRepository;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorService twoFactorService;
    private final TwoFactorNotifier notifier;

    public AuthController(
        AuthenticationManager authenticationManager,
        UserRepository userRepository,
        InMemoryTwoFactorRepository twoFactorRepository,
        PasswordEncoder passwordEncoder,
        TokenService tokenService,
        TwoFactorService twoFactorService,
        TwoFactorNotifier notifier
    ) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.twoFactorRepository = twoFactorRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.twoFactorService = twoFactorService;
        this.notifier = notifier;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody AuthRequestDTO dto) {
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new AuthResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO dto) {
        var exists = userRepository.findUserByEmail(dto.email());
        if (exists.isPresent())
          return ResponseEntity.badRequest().build();

        String code = twoFactorService.generateCode();
        TwoFactorDto token = new TwoFactorDto(
            code,
            Instant.now().plusSeconds(300),
            dto
            );

        twoFactorRepository.save(token);

        notifier.send(token);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/2fa")
    public ResponseEntity<?> verify(@RequestBody VerifyDTO dto) {
        var tokenExists = twoFactorRepository.findByEmail(dto.email());
        if (tokenExists.isEmpty())
          return ResponseEntity.status(404).body("Code not exists");

        var token = tokenExists.get();
        if (token.expiresAt().isBefore(Instant.now()))
            return ResponseEntity.status(401).body("Code expired");

        if (!token.code().equals(dto.code()))
            return ResponseEntity.status(401).body("Invalid code");


        String encryptedPassword = passwordEncoder.encode(token.dto().password());
        User user = new User(token.dto().name(), dto.email(), encryptedPassword, token.dto().role());
    
        userRepository.save(user);

        twoFactorRepository.deleteByEmail(dto.email());

        return ResponseEntity.ok().build();
    }

    @GetMapping("/ott/sent")
    public String getMethodName(@RequestParam String param) {
        return "sent";
    }
    
    
    
}
