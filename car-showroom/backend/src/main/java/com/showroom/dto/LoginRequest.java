package com.showroom.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String showroomCode,
                           @NotBlank String email,
                           @NotBlank String password) {
}
