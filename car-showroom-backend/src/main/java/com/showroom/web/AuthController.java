package com.showroom.web;

import com.showroom.dto.CurrentUserResponse;
import com.showroom.dto.LoginRequest;
import com.showroom.dto.LoginResponse;
import com.showroom.security.AuthUser;
import com.showroom.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return service.login(request);
    }

    /** Tokens are stateless: the client discards its token. Kept so the client has one call to make. */
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
    }

    @GetMapping("/me")
    public CurrentUserResponse me(@AuthenticationPrincipal AuthUser caller) {
        return service.me(caller);
    }
}
