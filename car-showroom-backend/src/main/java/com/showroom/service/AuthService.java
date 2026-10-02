package com.showroom.service;

import com.showroom.domain.AppUser;
import com.showroom.domain.Permission;
import com.showroom.domain.Role;
import com.showroom.domain.Showroom;
import com.showroom.dto.CurrentUserResponse;
import com.showroom.dto.LoginRequest;
import com.showroom.dto.LoginResponse;
import com.showroom.repo.AppUserRepository;
import com.showroom.repo.ShowroomRepository;
import com.showroom.security.AuthUser;
import com.showroom.security.JwtService;
import com.showroom.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String BAD_LOGIN = "Wrong showroom code, email or password.";

    private final ShowroomRepository showrooms;
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Transactional
    public LoginResponse login(LoginRequest r) {
        Showroom showroom = showrooms.findByCodeIgnoreCaseAndDeletedAtIsNull(r.showroomCode().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, BAD_LOGIN));
        AppUser user = users.findForLogin(showroom.getId(), r.email().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, BAD_LOGIN));
        if (user.getPasswordHash() == null || !encoder.matches(r.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, BAD_LOGIN);
        }
        if (!showroom.isActive() || !user.isActive()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "This account or showroom is inactive.");
        }
        user.setLastLoginAt(LocalDateTime.now());

        List<String> permissions = permissionCodes(user);
        String token = jwt.issue(user.getId(), showroom.getId(), user.getName(), permissions);
        return new LoginResponse(token, toResponse(user, showroom, permissions));
    }

    /** The caller's profile; permissions are the ones in their token (refreshed on next login). */
    @Transactional(readOnly = true)
    public CurrentUserResponse me(AuthUser caller) {
        AppUser user = users.findByIdAndShowroomIdAndDeletedAtIsNull(caller.userId(), caller.showroomId())
                .filter(AppUser::isActive)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please log in again."));
        Showroom showroom = showrooms.findById(caller.showroomId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please log in again."));
        return toResponse(user, showroom, caller.permissions().stream().sorted().toList());
    }

    /** Union of the permissions of all the user's (non-deleted) roles. */
    private static List<String> permissionCodes(AppUser user) {
        return user.getRoles().stream()
                .filter(role -> role.getDeletedAt() == null)
                .map(Role::getPermissions)
                .flatMap(java.util.Set::stream)
                .map(Permission::getCode)
                .distinct()
                .sorted()
                .toList();
    }

    private static CurrentUserResponse toResponse(AppUser u, Showroom s, List<String> permissions) {
        return new CurrentUserResponse(u.getId(), u.getName(), u.getEmail(), s.getId(), s.getCode(),
                s.getName(), u.getUserType().getCode(), permissions);
    }
}
