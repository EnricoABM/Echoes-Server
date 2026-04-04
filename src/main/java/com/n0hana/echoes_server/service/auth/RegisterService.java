package com.n0hana.echoes_server.service.auth;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.n0hana.echoes_server.dto.RegisterRequestDTO;
import com.n0hana.echoes_server.dto.TwoFactorDto;
import com.n0hana.echoes_server.dto.VerifyDTO;
import com.n0hana.echoes_server.model.User;
import com.n0hana.echoes_server.repository.InMemoryTwoFactorRepository;
import com.n0hana.echoes_server.repository.PendingRegisterRepository;
import com.n0hana.echoes_server.repository.UserRepository;
import com.n0hana.echoes_server.service.logs.Auditable;
import com.n0hana.echoes_server.service.notifier.LoggerNotifier;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RegisterService {
    
    private final UserRepository userRepository;
    private final TwoFactorService twoFactorService;
    private final LoggerNotifier notifier;
    private final InMemoryTwoFactorRepository twoFactorRepository;
    private final PasswordEncoder passwordEncoder;
    private final PendingRegisterRepository registerRepository;

    @Auditable(action = "Envio dos dados para registro", entity = "REGISTER")
    public String registerRequest(RegisterRequestDTO dto) {
        // Verifica se o usuário já existe
        
        if (userRepository.findUserByEmail(dto.email()).isPresent())
            throw new RuntimeException("E-mail ou Senha Inválidos");

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
        String password = passwordEncoder.encode(dto.password());
        dto = new RegisterRequestDTO(
            dto.name(),
            dto.email(),
            password,
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
        return "";
    }

    @Auditable(action = "Registro do usuário", entity = "REGISTER")
    public void registerMFA(VerifyDTO dto) {

        // Verifica se o código existe
        var tokenExists = twoFactorRepository.findByEmail(dto.email());
        if (tokenExists.isEmpty())
            throw new RuntimeException("Código Inválido");

        // Recolhe o código 
        var token = tokenExists.get();
        
        // Verifica se foi expirado
        if (token.expiresAt().isBefore(Instant.now()) || !token.code().equals(dto.code()))
            throw new RuntimeException("Código Inválido");


        // Recupera os dados de registro e verifica se existem
        RegisterRequestDTO registerDto = registerRepository.find(dto.email());
        if (registerDto == null)
            throw new RuntimeException("Código Inválido");

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
    }
}
