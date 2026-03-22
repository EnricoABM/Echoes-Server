package com.n0hana.echoes_server.service.password;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.n0hana.echoes_server.dto.PasswordDTO;
import com.n0hana.echoes_server.dto.TwoFactorDto;
import com.n0hana.echoes_server.model.User;
import com.n0hana.echoes_server.repository.UserRepository;
import com.n0hana.echoes_server.service.auth.JwtTokenService;
import com.n0hana.echoes_server.service.notifier.TwoFactorNotifier;
import com.n0hana.echoes_server.service.password.InMemoryPasswordCodeRepository.CodeType;
import com.n0hana.echoes_server.service.password.InMemoryPasswordCodeRepository.PasswordCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final TwoFactorNotifier emailNotifier;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();
    private final InMemoryPasswordCodeRepository codeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtService;

    public void requestReset(PasswordDTO.ForgotRequest dto) {
        Optional<User> opt = userRepository.findUserByEmail(dto.email());

        if (opt.isEmpty()) return;

        User user = opt.get();

        String code = this.generateRandomCode();

        PasswordCode passwordCode = codeRepository.new PasswordCode();
        passwordCode.setEmail(dto.email());
        passwordCode.setCode(code);
        passwordCode.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        passwordCode.setType(CodeType.RESET);

        codeRepository.save(passwordCode);
        emailNotifier.send(new TwoFactorDto(
            user.getEmail(), code, LocalDateTime.now().plusMinutes(15).toInstant(ZoneOffset.of("-03:00")))
        );
    }

    public void resetPassword(PasswordDTO.ResetRequest dto) {
        String code = dto.code();

        PasswordCode savedCode = codeRepository.getCode(dto.email());

        if (!code.equals(savedCode.getCode())) {
            throw new RuntimeException("Código de Redefinição de Senha Incorreto");
        }

        if (savedCode.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Código Expirado");
        }

        if (savedCode.getType().equals(CodeType.RESET)) {
            throw new RuntimeException("Código Incorreto");
        }

        System.out.printf(
            "new: '%s' (%d) | confirm: '%s' (%d)%n",
            dto.newPassword(),
            dto.newPassword().length(),
            dto.confirmPassword(),
            dto.confirmPassword().length()
        );

        if (!dto.newPassword().equals(dto.confirmPassword())) {
            throw new RuntimeException("As senhas não são indenticas");
        }

        User user = userRepository.findUserByEmail(dto.email()).orElseThrow( () ->
            new RuntimeException("E-mail não cadastrado")
        );

        codeRepository.delete(dto.email());
        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        userRepository.save(user);
    }

    public String validatePassword(PasswordDTO.ValidateRequest dto, String token) {

        token = token.replace("Bearer ", "");
        
        UUID uuid = UUID.fromString(jwtService.validadeToken(token));

        User user = userRepository.findById(uuid).orElseThrow( () ->
            new RuntimeException("Usuário não encontrado")
        );

        String password = user.getPassword();
        if (passwordEncoder.matches(dto.password(), password)) {
            return jwtService.generatePasswordChangeToken(user);
        } 
        return "";
    }

    public boolean changePassowrd(PasswordDTO.ChangeRequest dto) {
        if (!dto.newPassword().equals(dto.confirmPassword())) {
            return false;
        }

        String token = dto.token();
        token = token.replace("Bearer ", "");

        String uuid = jwtService.validatePasswordChangeToken(token);

        User user = userRepository.findById(UUID.fromString(uuid)).orElseThrow( () ->
            new RuntimeException("Usuário não encontrado")
        );

        user.setPassword(passwordEncoder.encode(dto.newPassword()));

        userRepository.save(user);

        return true;
    }

    private String generateRandomCode() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code); 
    }
    
}
