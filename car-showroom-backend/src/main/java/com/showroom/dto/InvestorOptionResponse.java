package com.showroom.dto;

import com.showroom.domain.AppUser;

/** A user that can be picked as an investor on the vehicle form or used as a list filter. */
public record InvestorOptionResponse(Long id, String name, String userTypeLabel, boolean active) {
    public static InvestorOptionResponse from(AppUser u) {
        return new InvestorOptionResponse(u.getId(), u.getName(), u.getUserType().getLabel(), u.isActive());
    }
}
