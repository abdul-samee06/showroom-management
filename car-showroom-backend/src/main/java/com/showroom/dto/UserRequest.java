package com.showroom.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * Create / edit a user. {@code password} is optional (a user without one cannot log in; on edit,
 * null keeps the current one). {@code roleIds} is only accepted from callers with USER_ASSIGN_ROLE.
 */
public record UserRequest(@NotBlank @Size(max = 150) String name,
                          @NotBlank String userTypeCode,
                          @Email @Size(max = 150) String email,
                          @Size(max = 30) String phone,
                          Boolean active,
                          @Size(min = 8, max = 72) String password,
                          Set<Long> roleIds) {
}
