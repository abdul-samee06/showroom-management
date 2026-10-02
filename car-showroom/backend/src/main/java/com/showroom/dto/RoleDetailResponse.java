package com.showroom.dto;

import java.util.List;

/** A role with its permission codes and the users who hold it (for the edit form). */
public record RoleDetailResponse(Long id, String name, String description, boolean system,
                                 List<String> permissionCodes, List<RoleUserResponse> users) {
}
