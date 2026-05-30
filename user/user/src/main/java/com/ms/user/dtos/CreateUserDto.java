package com.ms.user.dtos;

import com.ms.user.enums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDto(
    @NotBlank String name, 
    @NotBlank @Email String email, 
    @NotBlank String password, 
    RoleName role
) {}