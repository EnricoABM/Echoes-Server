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
import com.n0hana.echoes_server.repository.InMemoryTwoFactorRepository;
import com.n0hana.echoes_server.repository.PendingAuthRepository;
import com.n0hana.echoes_server.repository.PendingRegisterRepository;
import com.n0hana.echoes_server.repository.UserRepository;
import com.n0hana.echoes_server.service.auth.JwtTokenService;
import com.n0hana.echoes_server.service.auth.TwoFactorService;
import com.n0hana.echoes_server.service.notifier.EmailNotifier;
import com.n0hana.echoes_server.service.ratelimit.LoginAttemptService;

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
    private final EmailNotifier notifier;
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
        
        /**==================================
         *  AUTENTICAÇÃO PRIMÁRIA DO SISTEMA
         * ==================================
         * Valida as credenciais do usuário,
         * caso sejam invalidas, adiciona um
         * contandor de falhas consecutivas 
         * para bloqueio de tentativas de 
         * brute force.
        */
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

    /**==================================
     *  AUTENTICAÇÃO DE MULTI FATOR
     * ==================================
     * Após a autenticação primária o 
     * método é chamado para fornecidmento
     * do código único de validação
    */
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

        // BROWSER → COOKIE
        Cookie cookie = new Cookie("access_token", jwt);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS em produção
        cookie.setPath("/");
        cookie.setMaxAge(60 * 15);

        response.addCookie(cookie);

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
        
        /**==================================
         *  CRIPTOGRAFIA DE SENHA
         * ==================================
         * Criptografia das senha do usuário
         * usando algoritmo BCrypt.
         */  
        dto = new RegisterRequestDTO(
            dto.name(),
            dto.email(),
            passwordEncoder.encode(dto.password()),
            dto.role()
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

     /**==================================
     *   VALIDAÇÃO DE EMAIL CADASTRADO
     * ==================================
     * Após a autenticação primária o 
     * método é chamado para fornecidmento
     * do código único de validação
    */
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
            registerDto.role()
        );
    
        userRepository.save(user);

        // Limpa o dados do usuário da memória
        twoFactorRepository.deleteByEmail(dto.email());
        registerRepository.delete(registerDto.email());

        return ResponseEntity.ok().build();
    }    

    /**==================================
     *  INVALIDAÇÃO DE SESSÂO NO LOGOUT
     * ==================================
     * Busca os tokens armazenados do 
     * usuário e revoga a validade de 
     * todos.
    */
    @GetMapping("/logout")
    public ResponseEntity<Void> logout(
    HttpServletRequest request,
    HttpServletResponse response,
    @RequestHeader(value = "Authorization", required = false) String header) {
        String token = null;

        // API → Authorization header
        if (header != null && header.startsWith("Bearer ")) {
            token = header.replace("Bearer ", "");
        }

        // Browser → Cookie
        if (token == null && request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookie.getName().equals("access_token")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        if (token == null) {
            return ResponseEntity.badRequest().build();
        }

        String uuid = tokenService.validadeToken(token);

        Optional<User> opt = userRepository.findById(UUID.fromString(uuid));
    
        if (opt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Revoga todos os tokens do usuário
        User user = opt.get();

        // revoga tokens ativos
        tokenService.revokeAll(user);

        // remove cookie do navegador
        Cookie cookie = new Cookie("access_token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS em produção
        cookie.setPath("/");
        cookie.setMaxAge(0);

        response.addCookie(cookie);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate-token")
    public ResponseEntity<Void> authMe() {
        return ResponseEntity.ok().build();
    }
}
