package com.showroom.dto;

import com.showroom.domain.Permission;

public record PermissionResponse(String code, String label, String module, String urlPattern) {
    public static PermissionResponse from(Permission p) {
        return new PermissionResponse(p.getCode(), p.getLabel(), p.getModule(), p.getUrlPattern());
    }
}
