package com.ms.user.controllers;

import com.ms.user.dtos.EmailDto;
import com.ms.user.enums.RoleName;
import com.ms.user.models.Role;
import com.ms.user.models.UserModel;
import com.ms.user.producers.UserProducer;
import com.ms.user.repositories.UserRepository;
import com.ms.user.services.CodigoCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CodigoCacheService cacheService;
    @Autowired private UserProducer userProducer;

    // Criando um DTO rápido para receber o email do Postman
    public record RequestCodeDto(String email) {}

    @PostMapping("/request-code")
    public ResponseEntity<String> requestCode(@RequestBody RequestCodeDto dto) {
        String email = dto.email();

        // 1. Gera código aleatório de 6 dígitos 
        String codigo = String.format("%06d", new Random().nextInt(1000000));

        // 2. Salva o código na memória (Cache) por 5 minutos 
        cacheService.salvarCodigo(email, codigo);

        // 3. Verifica se o e-mail já existe. Se não existir, cria um usuário temporário [cite: 41]
        Optional<UserModel> userOpt = userRepository.findByEmail(email);
        UserModel user;
        
        if (userOpt.isEmpty()) {
            user = new UserModel();
            user.setEmail(email);
            user.setName("Usuário Temporário"); // Sem nome real [cite: 41]
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Senha aleatória [cite: 41]
            
            Role role = new Role();
            role.setName(RoleName.ROLE_CUSTOMER); // Role CUSTOMER [cite: 41]
            user.setRoles(List.of(role));
            
            user = userRepository.save(user); // Salva no MySQL
        } else {
            user = userOpt.get(); // Se já existe, apenas pega do banco
        }

        // 4. Monta o envelope do e-mail com assunto e texto 
        EmailDto emailDto = new EmailDto(
                user.getUserId(),
                email,
                "Seu código de acesso",
                "Seu código de acesso é: " + codigo
        );

        // 5. Publica a mensagem na fila do RabbitMQ 
        userProducer.publishMessageEmail(emailDto);

        // 6. Retorna 200 OK sem mostrar o código na resposta (segurança) [cite: 55, 58]
        return ResponseEntity.ok("Código solicitado com sucesso. Verifique seu e-mail.");
    }
}