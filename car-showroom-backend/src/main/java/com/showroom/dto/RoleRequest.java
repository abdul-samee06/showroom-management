package com.showroom.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RoleRequest(@NotBlank @Size(max = 60) String name,
                          @Size(max = 255) String description,
                          Set<String> permissionCodes) {
}
