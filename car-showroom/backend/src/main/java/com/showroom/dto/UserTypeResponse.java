package com.showroom.dto;

import com.showroom.domain.UserType;

public record UserTypeResponse(String code, String label) {
    public static UserTypeResponse from(UserType t) {
        return new UserTypeResponse(t.getCode(), t.getLabel());
    }
}
