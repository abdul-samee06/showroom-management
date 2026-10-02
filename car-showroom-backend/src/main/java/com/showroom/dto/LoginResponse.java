package com.showroom.dto;

public record LoginResponse(String token, CurrentUserResponse user) {
}
