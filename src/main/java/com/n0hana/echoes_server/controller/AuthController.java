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
import com.n0hana.echoes_server.repository.PendingAuthRepository;
import com.n0hana.echoes_server.repository.PendingRegisterRepository;
import com.n0hana.echoes_server.repository.UserRepository;
import com.n0hana.echoes_server.service.LoggerNotifier;
import com.n0hana.echoes_server.service.TokenService;
import com.n0hana.echoes_server.service.TwoFactorService;

import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final InMemoryTwoFactorRepository twoFactorRepository;
    private final PendingRegisterRepository registerRepository;
    private final PendingAuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final TwoFactorService twoFactorService;
    private final LoggerNotifier notifier;


    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody AuthRequestDTO dto) {
        var exists = userRepository.findUserByEmail(dto.email());
        if (exists.isEmpty())
            return ResponseEntity.badRequest().build();

        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        this.authenticationManager.authenticate(usernamePassword);

        String code = twoFactorService.generateCode();
        TwoFactorDto token = new TwoFactorDto(
            dto.email(),
            code,
            Instant.now().plusSeconds(300)
        );

        authRepository.save(dto);
        twoFactorRepository.save(token);

        notifier.send(token);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<?> loginMFA(@RequestBody VerifyDTO dto) {
        var tokenExists = twoFactorRepository.findByEmail(dto.email());
        if (tokenExists.isEmpty())
            return ResponseEntity.status(404).body("Code not exists");
        var token = tokenExists.get();
        if (token.expiresAt().isBefore(Instant.now()))
            return ResponseEntity.status(401).body("Code expired");

        if (!token.code().equals(dto.code()))
            return ResponseEntity.status(401).body("Invalid code");
        var auth = authRepository.find(dto.email());
        if (auth == null)
            return ResponseEntity.status(404).body("Code not exists");

        var usernamePassword = new UsernamePasswordAuthenticationToken(auth.email(), auth.password());
        var authToken = this.authenticationManager.authenticate(usernamePassword);

        var jwt = tokenService.generateToken((User) authToken.getPrincipal());

        authRepository.delete(dto.email());

        return ResponseEntity.ok(new AuthResponseDTO(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO dto) {
        var exists = userRepository.findUserByEmail(dto.email());
        if (exists.isPresent())
          return ResponseEntity.badRequest().build();

        String code = twoFactorService.generateCode();
        TwoFactorDto token = new TwoFactorDto(
            dto.email(),
            code,
            Instant.now().plusSeconds(300)
            );
        registerRepository.save(dto);

        twoFactorRepository.save(token);

        notifier.send(token);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/2fa")
    public ResponseEntity<?> registerMFA(@RequestBody VerifyDTO dto) {
        var tokenExists = twoFactorRepository.findByEmail(dto.email());
        if (tokenExists.isEmpty())
            return ResponseEntity.status(404).body("Code not exists");

        var token = tokenExists.get();
        if (token.expiresAt().isBefore(Instant.now()))
            return ResponseEntity.status(401).body("Code expired");

        if (!token.code().equals(dto.code()))
            return ResponseEntity.status(401).body("Invalid code");
        
        var registerDto = registerRepository.find(dto.email());
        if (registerDto == null)
            return ResponseEntity.status(404).body("Code not exists");


        String encryptedPassword = passwordEncoder.encode(registerDto.password());
        User user = new User(registerDto.name(), dto.email(), encryptedPassword, registerDto.role());
    
        userRepository.save(user);

        twoFactorRepository.deleteByEmail(dto.email());
        registerRepository.delete(registerDto.email());

        return ResponseEntity.ok().build();
    }    

    
}
