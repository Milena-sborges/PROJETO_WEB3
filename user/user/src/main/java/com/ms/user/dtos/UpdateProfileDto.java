

package com.ms.user.dtos;

public record UpdateProfileDto(
    String name, 
    String role // Mudamos de RoleName para String
) {}