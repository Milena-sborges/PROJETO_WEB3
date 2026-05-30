package com.ms.user.services;

import com.ms.user.dtos.CreateUserDto;
import com.ms.user.dtos.LoginUserDto;
import com.ms.user.dtos.RecoveryJwtTokenDto;
import com.ms.user.models.Role;
import com.ms.user.models.UserModel;
import com.ms.user.repositories.UserRepository;
import com.ms.user.security.service.JwtTokenService;
import com.ms.user.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtTokenService jwtTokenService;

    public UserModel save(CreateUserDto dto) {
        var userModel = UserModel.builder()
            .name(dto.name())
            .email(dto.email())
            .password(passwordEncoder.encode(dto.password()))
            .roles(List.of(Role.builder().name(dto.role()).build()))
            .build();
        return userRepository.save(userModel);
    }

    public RecoveryJwtTokenDto authenticateUser(LoginUserDto loginDto) {
        var authToken = new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password());
        var authentication = authenticationManager.authenticate(authToken);
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return new RecoveryJwtTokenDto(jwtTokenService.generateToken(userDetails));
    }
}