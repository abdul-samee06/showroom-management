package com.showroom.dto;

import com.showroom.domain.Role;

/** Small role shape for dropdowns, filters and the Roles column of the users list. */
public record RoleOptionResponse(Long id, String name, boolean system) {
    public static RoleOptionResponse from(Role r) {
        return new RoleOptionResponse(r.getId(), r.getName(), r.isSystem());
    }
}
