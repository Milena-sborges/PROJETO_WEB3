package com.ms.user.controllers;

import com.ms.user.dtos.CreateUserDto;
import com.ms.user.dtos.LoginUserDto;
import com.ms.user.dtos.RecoveryJwtTokenDto;
import com.ms.user.models.UserModel;
import com.ms.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<UserModel> createUser(@RequestBody CreateUserDto dto) {
        UserModel createdUser = userService.save(dto);
        // Retorna o status 201 (Created) quando o usuário é salvo com sucesso
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/login")
    public ResponseEntity<RecoveryJwtTokenDto> login(@RequestBody LoginUserDto dto) {
        RecoveryJwtTokenDto token = userService.authenticateUser(dto);
        // Retorna o token JWT
        return ResponseEntity.ok(token);
    }

    @GetMapping("/test/customer")
    public ResponseEntity<String> customerTest() {
        // Esse endpoint só funciona se a pessoa mandar o Token no Postman
        return ResponseEntity.ok("Acesso de CUSTOMER autorizado com sucesso!");
    }
}