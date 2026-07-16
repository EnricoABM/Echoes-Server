package com.n0hana.echoes_server.register;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.n0hana.echoes_server.infra.config.SecurityConfig;
import com.n0hana.echoes_server.mfa.VerifyDTO;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller para Registro de Usuários.
 *
 * <p>
 * Expoẽs os endpoints para gerenciar operações de registro.
 * </p>
 *
 * @apiNote {@code /api/register/}
 * @apiNote {@code /api/register/admin}
 * @apiNote {@code /api/register/2fa}
 * 
 * @author Enrico Bertozzi
 * @since 1.0
 */
@RestController
@RequestMapping("/api/register/")
@SecurityRequirement(name = SecurityConfig.SECURITY)
@RequiredArgsConstructor
public class RegisterController {
    // TODO: Adicionar resposta de erro ao usuário. 

    private final RegisterService registerService;

    /**
     * Endpoint público Registro de usuário {@link Teacher} e {@link Student}.
     *
     * @param dto - DTO com dados de registro.
     * 
     * @apiNote 200 OK - Registro realizado.
     * @apiNote 400 BAD REQUEST - Falha ao registrar.
     */
    @PostMapping("/")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequestDTO dto) {
        try {
            registerService.pendingRegister(dto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint protegitdo para registro de usuário {@code Admin}.
     *
     * <p> Necessário role {@code ADMIN}.
     * 
     * @param dto - DTO com dados de registro.
     *
     * @apiNote 200 OK - Registro realizado.
     * @apiNote 400 BAD REQUEST - Falha ao registrar.
     */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> registerAdmin(@RequestBody @Valid RegisterRequestDTO dto) {
        try {
            registerService.pendingRegisterAdmin(dto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Endpoint público para validação de código multifator no registro.
     *
     * @param dto - DTO com o código multifator
     *
     * @apiNote 200 OK - Código validado com sucesso.
     * @apiNote 400 BAD REQUEST - Código inválido.
     */
    @PostMapping("/2fa")
    public ResponseEntity<Void> registerMFA(@RequestBody VerifyDTO dto) { 
        try {
            registerService.registerMFA(dto);
            return ResponseEntity.ok().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
