package com.n0hana.echoes_server.controller;

import java.time.Instant;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.n0hana.echoes_server.dto.AuthRequestDTO;
import com.n0hana.echoes_server.dto.AuthResponseDTO;
import com.n0hana.echoes_server.dto.RegisterRequestDTO;
import com.n0hana.echoes_server.dto.TwoFactorDto;
import com.n0hana.echoes_server.dto.VerifyDTO;
import com.n0hana.echoes_server.model.User;
import com.n0hana.echoes_server.model.UserRole;
import com.n0hana.echoes_server.repository.InMemoryTwoFactorRepository;
import com.n0hana.echoes_server.repository.PendingAuthRepository;
import com.n0hana.echoes_server.repository.PendingRegisterRepository;
import com.n0hana.echoes_server.repository.UserRepository;
import com.n0hana.echoes_server.service.auth.JwtTokenService;
import com.n0hana.echoes_server.service.auth.TwoFactorService;
import com.n0hana.echoes_server.service.notifier.LoggerNotifier;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenService tokenService;
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

        // Verifica se o Usuário existe
        var exists = userRepository.findUserByEmail(dto.email());
        if (exists.isEmpty())
            return ResponseEntity.badRequest().build();

        // Autenticação primária
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        this.authenticationManager.authenticate(usernamePassword);

        // Criação do Código de 2FA
        String code = twoFactorService.generateCode();

        TwoFactorDto token = new TwoFactorDto(
            dto.email(),
            code,
            Instant.now().plusSeconds(300)
        );

        // Salva dados do login
        authRepository.save(dto);

        // Sava o código 2FA
        twoFactorRepository.save(token);

        // Envia Notificação
        notifier.send(token);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/login/2fa")
    public ResponseEntity<?> loginMFA(
            @RequestBody VerifyDTO dto,
            HttpServletRequest request,
            HttpServletResponse response) {

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

        var usernamePassword =
            new UsernamePasswordAuthenticationToken(auth.email(), auth.password());

        var authToken = authenticationManager.authenticate(usernamePassword);

        var jwt = tokenService.generateToken((User) authToken.getPrincipal());

        authRepository.delete(dto.email());
        twoFactorRepository.deleteByEmail(dto.email());

        String accept = request.getHeader("Accept");

        // CLIENTE REST
        if (accept != null && accept.contains("application/json")) {
            return ResponseEntity.ok(new AuthResponseDTO(jwt));
        }

        // BROWSER → COOKIE
        Cookie cookie = new Cookie("access_token", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS em produção
        cookie.setPath("/");
        cookie.setMaxAge(60 * 15);

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDTO dto) {
        
        // Verifica se o usuário já existe
        var exists = userRepository.findUserByEmail(dto.email());

        if (exists.isPresent())
          return ResponseEntity.badRequest().build();

        // Gera código de 2FA
        String code = twoFactorService.generateCode();
        TwoFactorDto token = new TwoFactorDto(
            dto.email(),
            code,
            Instant.now().plusSeconds(300)
        );
        
        dto = new RegisterRequestDTO(
            dto.name(),
            dto.email(),
            passwordEncoder.encode(dto.password())
        );

        // Salva os dados do registro
        registerRepository.save(
            dto
        );

        // Salva os dados do 2FA
        twoFactorRepository.save(token);

        // Envia o código
        notifier.send(token);
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register/2fa")
    public ResponseEntity<?> registerMFA(@RequestBody VerifyDTO dto) {

        // Verifica se o código existe
        var tokenExists = twoFactorRepository.findByEmail(dto.email());
        if (tokenExists.isEmpty())
            return ResponseEntity.status(404).body("Code not exists");

        // Recolhe o código 
        var token = tokenExists.get();
        
        // Verifica se foi expirado
        if (token.expiresAt().isBefore(Instant.now()))
            return ResponseEntity.status(401).body("Code expired");

        // Verificação se o código fornecido é o mesmo recuperado
        if (!token.code().equals(dto.code()))
            return ResponseEntity.status(401).body("Invalid code");
        
        // Recupera os dados de registro e verifica se existem
        var registerDto = registerRepository.find(dto.email());
        if (registerDto == null)
            return ResponseEntity.status(404).body("Code not exists");

        
        
        // Cria o novo usuário
        User user = new User(
            registerDto.name(), 
            dto.email(), 
            registerDto.password(), 
            UserRole.TEACHER
        );
    
        userRepository.save(user);

        // Limpa o dados do usuário da memória
        twoFactorRepository.deleteByEmail(dto.email());
        registerRepository.delete(registerDto.email());

        return ResponseEntity.ok().build();
    }    

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String header) {
        String token = header.replace("Bearer ", "");

        String email = tokenService.validadeToken(token);

        Optional<User> opt = userRepository.findUserByEmail(email);
    
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = opt.get();

        tokenService.revokeAll(user);

        return ResponseEntity.ok().build();
    }
}
