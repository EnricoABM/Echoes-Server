package com.n0hana.echoes_server.controller;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
import com.n0hana.echoes_server.dto.LoginFailedResponseDTO;
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
import com.n0hana.echoes_server.service.ratelimit.LoginAttemptService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;




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
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO dto) {

        // Verifica se o Usuário existe
        var exists = userRepository.findUserByEmail(dto.email());
        if (exists.isEmpty())
            return ResponseEntity.badRequest().body("User not found");

        // Retorna o usuário
        User user = exists.get();

        // Verifica se a conta está bloqueada ou não
        if (!user.isAccountNonLocked()) {
            return ResponseEntity.status(423).body("Account Locked");
        }

        // Autenticação primária
        var usernamePassword = new UsernamePasswordAuthenticationToken(dto.email(), dto.password());
        
        try {
            // Tenta autenticar o usuário com email e senha
            this.authenticationManager.authenticate(usernamePassword);
            loginAttemptService.loginSucceeded(dto.email());

        } catch (BadCredentialsException e) {
            // Caso as credenciais sejam invalidas, adiciona um contador na quantidade máxima
            int attempts = loginAttemptService.loginFailed(dto.email());

            // Calcula a quantidade restante
            int remainingAttempts = loginAttemptService.MAX_ATTEMPTS - attempts;

            // Resposta da autenticação inválida, informa quantidade restante
            return ResponseEntity.status(401).body(
                new LoginFailedResponseDTO(
                    "Invalid credentials",
                    attempts,
                    Math.max(remainingAttempts, 0)
                )
            );
        }

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
    public ResponseEntity<?> loginMFA(@RequestBody VerifyDTO dto) {
        // Busca o token cadastrado
        var tokenExists = twoFactorRepository.findByEmail(dto.email());

        // Verifica se o token existe
        if (tokenExists.isEmpty())
            return ResponseEntity.status(404).body("Code not exists");
        
        // Recolhe o token
        var token = tokenExists.get();

        // Verificações sobre o token
        // Verifica se foi expirado
        if (token.expiresAt().isBefore(Instant.now()))
            return ResponseEntity.status(401).body("Code expired");

        // Verificação se o token fornecido é o mesmo recuperado
        if (!token.code().equals(dto.code()))
            return ResponseEntity.status(401).body("Invalid code");
        
        // Busca os dados do login
        var auth = authRepository.find(dto.email());
        if (auth == null)
            return ResponseEntity.status(404).body("Code not exists");

        // Autenticação 
        var usernamePassword = new UsernamePasswordAuthenticationToken(auth.email(), auth.password());
        var authToken = this.authenticationManager.authenticate(usernamePassword);

        // Criação do Token JWT
        var jwt = tokenService.generateToken((User) authToken.getPrincipal());

        // Remove dados salvos do usuário da memoria
        authRepository.delete(dto.email());
        twoFactorRepository.deleteByEmail(dto.email());

        return ResponseEntity.ok(new AuthResponseDTO(jwt));
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

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String header) {
        
        // Verifica se o usuário existe
        String token = header.replace("Bearer ", "");

        String uuid = tokenService.validadeToken(token);

        Optional<User> opt = userRepository.findById(UUID.fromString(uuid));
    
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Revoga todos os tokens do usuário
        User user = opt.get();

        tokenService.revokeAll(user);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Void> authMe() {
        return ResponseEntity.ok().build();
    }
    
}
