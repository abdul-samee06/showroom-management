package com.showroom.dto;

import com.showroom.domain.AppUser;

import java.util.Comparator;
import java.util.List;

public record UserResponse(Long id, String name, String userTypeCode, String userTypeLabel,
                           String email, String phone, boolean active, boolean canLogin,
                           List<RoleOptionResponse> roles) {

    public static UserResponse from(AppUser u) {
        List<RoleOptionResponse> roles = u.getRoles().stream()
                .filter(r -> r.getDeletedAt() == null)
                .sorted(Comparator.comparing(r -> r.getName().toLowerCase()))
                .map(RoleOptionResponse::from)
                .toList();
        return new UserResponse(u.getId(), u.getName(), u.getUserType().getCode(),
                u.getUserType().getLabel(), u.getEmail(), u.getPhone(), u.isActive(),
                u.getPasswordHash() != null, roles);
    }
}
