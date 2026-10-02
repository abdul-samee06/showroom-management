package com.showroom.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(@NotNull Boolean active) {
}
