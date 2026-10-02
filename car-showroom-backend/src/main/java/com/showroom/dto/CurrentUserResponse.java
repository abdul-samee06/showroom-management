package com.showroom.dto;

import java.util.List;

/** The logged-in user with the permission codes loaded from all of their roles. */
public record CurrentUserResponse(Long id, String name, String email,
                                  Long showroomId, String showroomCode, String showroomName,
                                  String userTypeCode, List<String> permissions) {
}
