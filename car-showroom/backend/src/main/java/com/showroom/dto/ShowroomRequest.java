package com.showroom.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Creates a showroom together with its built-in Admin role and first admin user. */
public record ShowroomRequest(
        @NotBlank @Size(min = 2, max = 30) @Pattern(regexp = "[A-Za-z0-9_-]+",
                message = "may contain only letters, digits, - and _") String code,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 30) String phone,
        @Size(max = 150) String email,
        @Size(max = 255) String address,
        @NotBlank @Size(max = 150) String adminName,
        @NotBlank @Email @Size(max = 150) String adminEmail,
        @NotBlank @Size(min = 8, max = 72) String adminPassword) {
}
