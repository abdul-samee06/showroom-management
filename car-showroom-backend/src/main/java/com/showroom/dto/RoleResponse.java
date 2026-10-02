package com.showroom.dto;

import com.showroom.domain.Role;

/** One row of the roles list. */
public record RoleResponse(Long id, String name, String description, boolean system,
                           int permissionCount, long userCount) {
    public static RoleResponse from(Role r, long userCount) {
        return new RoleResponse(r.getId(), r.getName(), r.getDescription(), r.isSystem(),
                r.getPermissions().size(), userCount);
    }
}
