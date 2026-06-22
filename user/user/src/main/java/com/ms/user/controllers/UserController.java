package com.ms.user.controllers;

import com.ms.user.dtos.CreateUserDto;
import com.ms.user.dtos.LoginUserDto;
import com.ms.user.dtos.RecoveryJwtTokenDto;
import com.ms.user.models.UserModel;
import com.ms.user.services.UserService;
import com.ms.user.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.ms.user.dtos.UpdateProfileDto;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepository;

    @PostMapping
    public ResponseEntity<UserModel> createUser(@RequestBody CreateUserDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<RecoveryJwtTokenDto> login(@RequestBody LoginUserDto dto) {
        return ResponseEntity.ok(userService.authenticateUser(dto));
    }

    @GetMapping("/test/customer")
    public ResponseEntity<String> customerTest() {
        return ResponseEntity.ok("Acesso de CUSTOMER autorizado com sucesso!");
    }

    @PostMapping("/update-profile")
    @Transactional
    public ResponseEntity<Object> updateProfile(Authentication authentication, @RequestBody UpdateProfileDto dto) {
        String email = authentication.getName(); 
        UserModel updatedUser = userService.updateProfile(email, dto);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ResponseEntity<?> getMe(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            
            // Força carregamento das roles
            if (user.getRoles() != null) {
                user.getRoles().size();
            }
            
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Erro: " + e.getMessage());
        }
    }
}