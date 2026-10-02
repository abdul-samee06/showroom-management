package com.showroom.dto;

import com.showroom.domain.Showroom;

public record ShowroomResponse(Long id, String code, String name) {
    public static ShowroomResponse from(Showroom s) {
        return new ShowroomResponse(s.getId(), s.getCode(), s.getName());
    }
}
