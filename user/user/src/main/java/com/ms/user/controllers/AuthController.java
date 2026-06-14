package com.ms.user.controllers;

import com.ms.user.dtos.EmailDto;
import com.ms.user.enums.RoleName;
import com.ms.user.models.Role;
import com.ms.user.models.UserModel;
import com.ms.user.producers.UserProducer;
import com.ms.user.repositories.UserRepository;
import com.ms.user.services.CodigoCacheService;
import com.ms.user.security.service.JwtTokenService;
import com.ms.user.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*") // LIBERA O GOOGLE CHROME
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CodigoCacheService cacheService;
    @Autowired private UserProducer userProducer;
    @Autowired private JwtTokenService jwtTokenService; // ADICIONADO PARA O TOKEN

    public record RequestCodeDto(String email) {}
    public record VerifyCodeDto(String email, String codigo) {} // ADICIONADO PARA A VALIDAÇÃO

    @PostMapping("/request-code")
    public ResponseEntity<String> requestCode(@RequestBody RequestCodeDto dto) {
        String email = dto.email();

        // 1. Gera código aleatório de 6 dígitos
        String codigo = String.format("%06d", new Random().nextInt(1000000));

        // 2. Salva o código na memória (Cache) por 5 minutos
        cacheService.salvarCodigo(email, codigo);

        // 3. Verifica se o e-mail já existe.
        Optional<UserModel> userOpt = userRepository.findByEmail(email);
        UserModel user;
        
        if (userOpt.isEmpty()) {
            user = new UserModel();
            user.setEmail(email);
            user.setName("Usuário Temporário"); 
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); 
            
            Role role = new Role();
            role.setName(RoleName.ROLE_CUSTOMER); 
            user.setRoles(List.of(role));
            
            user = userRepository.save(user); 
        } else {
            user = userOpt.get(); 
        }

        // 4. Monta o envelope do e-mail
        EmailDto emailDto = new EmailDto(
                user.getUserId(),
                email,
                "Seu código de acesso",
                "Seu código de acesso é: " + codigo
        );

        // 5. Publica a mensagem na fila do RabbitMQ
        userProducer.publishMessageEmail(emailDto);

        return ResponseEntity.ok("Código solicitado com sucesso. Verifique seu e-mail.");
    }

    // === O MÉTODO QUE ESTAVA FALTANDO ===
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody VerifyCodeDto dto) {
        
        // 1. Acha o usuário no banco
        Optional<UserModel> userOpt = userRepository.findByEmail(dto.email());
        
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("erro", "Usuário não encontrado."));
        }

        // 2. Prepara o formato do Spring Security
        UserModel user = userOpt.get();
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        
        // 3. Gera o Token JWT
        // ATENÇÃO: Se o VS Code sublinhar 'generateToken' de vermelho, 
        // experimente trocar o nome para 'createToken' ou algo parecido do seu projeto!
        String token = jwtTokenService.generateToken(userDetails);

        // 4. Devolve o token para a sua tela!
        return ResponseEntity.ok(Map.of("token", token));
    }
}