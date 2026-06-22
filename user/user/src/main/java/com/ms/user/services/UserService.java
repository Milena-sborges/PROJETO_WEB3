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
import org.springframework.transaction.annotation.Transactional;
import com.ms.user.enums.RoleName;
import java.util.ArrayList;
import java.util.List;
import com.ms.user.dtos.UpdateProfileDto;

@Service
public class UserService {
    
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private JwtTokenService jwtTokenService;

    public UserModel save(CreateUserDto dto) {
        RoleName cargoPadrao = dto.role() != null ? dto.role() : RoleName.ROLE_CUSTOMER;

        var userModel = UserModel.builder()
            .name(dto.name())
            .email(dto.email())
            .password(passwordEncoder.encode(dto.password()))
            .roles(List.of(Role.builder().name(cargoPadrao).build()))
            .build();
            
        return userRepository.save(userModel);
    }

    public RecoveryJwtTokenDto authenticateUser(LoginUserDto loginDto) {
        var authToken = new UsernamePasswordAuthenticationToken(loginDto.email(), loginDto.password());
        var authentication = authenticationManager.authenticate(authToken);
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return new RecoveryJwtTokenDto(jwtTokenService.generateToken(userDetails));
    }

    @Transactional
    public UserModel updateProfile(String email, UpdateProfileDto dto) {
        UserModel user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        
        if (dto.name() != null && !dto.name().isBlank()) {
            user.setName(dto.name());
        }
        
        if (dto.role() != null) {
            Role role = new Role();
            if (dto.role().equals("ROLE_ADMINISTRATOR")) {
                role.setName(RoleName.ROLE_ADMINISTRATOR);
            } else {
                role.setName(RoleName.ROLE_CUSTOMER); 
            }
            
            List<Role> novasRoles = new ArrayList<>();
            novasRoles.add(role);
            user.setRoles(novasRoles);
        }
        
        return userRepository.save(user);
    }
}