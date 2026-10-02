package com.showroom.web;

import com.showroom.dto.PermissionResponse;
import com.showroom.dto.RoleDetailResponse;
import com.showroom.dto.RoleOptionResponse;
import com.showroom.dto.RoleRequest;
import com.showroom.dto.RoleResponse;
import com.showroom.security.AuthUser;
import com.showroom.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService service;

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public List<RoleResponse> list(@AuthenticationPrincipal AuthUser caller) {
        return service.list(caller.showroomId());
    }

    /** Role names only: feeds the Roles field of the user form and the role filter of the users list. */
    @GetMapping("/roles/options")
    @PreAuthorize("hasAnyAuthority('ROLE_VIEW', 'USER_VIEW', 'USER_ASSIGN_ROLE')")
    public List<RoleOptionResponse> options(@AuthenticationPrincipal AuthUser caller) {
        return service.options(caller.showroomId());
    }

    @GetMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public RoleDetailResponse get(@AuthenticationPrincipal AuthUser caller, @PathVariable Long id) {
        return service.get(caller.showroomId(), id);
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('ROLE_VIEW')")
    public List<PermissionResponse> permissions() {
        return service.allPermissions();
    }

    @PostMapping("/roles")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public RoleDetailResponse create(@AuthenticationPrincipal AuthUser caller,
                                     @Valid @RequestBody RoleRequest request) {
        return service.create(caller.showroomId(), request);
    }

    @PutMapping("/roles/{id}")
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public RoleDetailResponse update(@AuthenticationPrincipal AuthUser caller, @PathVariable Long id,
                                     @Valid @RequestBody RoleRequest request) {
        return service.update(caller.showroomId(), id, request);
    }

    @DeleteMapping("/roles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('ROLE_MANAGE')")
    public void delete(@AuthenticationPrincipal AuthUser caller, @PathVariable Long id) {
        service.delete(caller.showroomId(), id);
    }
}
