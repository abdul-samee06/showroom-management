package com.showroom.security;

import java.util.Set;

/** The authenticated caller, rebuilt from the JWT on every request. */
public record AuthUser(Long userId, Long showroomId, String name, Set<String> permissions) {
    public boolean has(String permissionCode) {
        return permissions.contains(permissionCode);
    }
}
