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
import java.util.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CodigoCacheService cacheService;
    @Autowired private UserProducer userProducer;
    @Autowired private JwtTokenService jwtTokenService;

    @PostMapping("/request-code")
    public ResponseEntity<?> requestCode(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            System.out.println("Solicitando código para: " + email);
            
            String codigo = String.format("%06d", new Random().nextInt(1000000));
            cacheService.salvarCodigo(email, codigo);

            Optional<UserModel> userOpt = userRepository.findByEmail(email);
            UserModel user;
            
            if (userOpt.isEmpty()) {
                System.out.println("Criando novo usuário temporário");
                user = new UserModel();
                user.setEmail(email);
                user.setName("Usuário Temporário");
                user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                
                // Criar role com lista mutável
                Role role = new Role();
                role.setName(RoleName.ROLE_CUSTOMER);
                
                List<Role> listaRoles = new ArrayList<>();
                listaRoles.add(role);
                user.setRoles(listaRoles);
                
                user = userRepository.save(user);
                System.out.println("Usuário criado com ID: " + user.getUserId());
            } else {
                user = userOpt.get();
                System.out.println("Usuário já existe: " + user.getUserId());
            }

            // Criar EmailDto e publicar
            EmailDto emailDto = new EmailDto(
                user.getUserId(),
                email,
                "Seu código de acesso",
                "Seu código de acesso é: " + codigo
            );
            
            userProducer.publishMessageEmail(emailDto);
            System.out.println("Mensagem publicada na fila");

            return ResponseEntity.ok("Código solicitado com sucesso. Verifique seu e-mail.");
            
        } catch (Exception e) {
            System.err.println("ERRO NO REQUEST-CODE: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        try {
            String email = body.get("email");
            String codigo = body.get("codigo");
            
            if (codigo == null) {
                codigo = body.get("code");
            }

            System.out.println("Verificando código para: " + email);
            System.out.println("Código recebido: " + codigo);

            String codigoSalvo = cacheService.obterCodigo(email);
            System.out.println("Código salvo: " + codigoSalvo);

            if (codigoSalvo == null || !codigoSalvo.equals(codigo)) {
                return ResponseEntity.status(401)
                    .body(Map.of("erro", "Código inválido"));
            }

            UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            UserDetailsImpl userDetails = new UserDetailsImpl(user);
            String token = jwtTokenService.generateToken(userDetails);

            return ResponseEntity.ok(Map.of("token", token));
            
        } catch (Exception e) {
            System.err.println("ERRO NO VERIFY-CODE: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("erro", e.getMessage()));
        }
    }
}