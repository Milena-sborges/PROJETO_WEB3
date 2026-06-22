package com.ms.user.dtos;

public record EmailDto(
    Long userId,
    String emailTo,
    String subject,
    String text
) {}