package com.ms.user.dtos;

public record LoginUserDto(
    String email,
    String password
) {}