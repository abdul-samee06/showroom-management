package com.showroom.web;

import com.showroom.dto.UserRequest;
import com.showroom.dto.UserResponse;
import com.showroom.dto.UserStatusRequest;
import com.showroom.dto.UserTypeResponse;
import com.showroom.dto.VehicleResponse;
import com.showroom.security.AuthUser;
import com.showroom.service.UserService;
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
public class UserController {
    private final UserService service;

    /** Needed by the user form; any logged-in user may read the list of types. */
    @GetMapping("/user-types")
    public List<UserTypeResponse> types() {
        return service.types();
    }

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public List<UserResponse> list(@AuthenticationPrincipal AuthUser caller,
                                   @RequestParam(required = false) String type,
                                   @RequestParam(required = false) Long roleId) {
        return service.list(caller.showroomId(), type, roleId);
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USER_CREATE')")
    public UserResponse create(@AuthenticationPrincipal AuthUser caller,
                               @Valid @RequestBody UserRequest request) {
        return service.create(caller, request);
    }

    /** Role changes in the body additionally need USER_ASSIGN_ROLE (checked in the service). */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasAuthority('USER_EDIT')")
    public UserResponse update(@AuthenticationPrincipal AuthUser caller, @PathVariable Long id,
                               @Valid @RequestBody UserRequest request) {
        return service.update(caller, id, request);
    }

    @PatchMapping("/users/{id}/status")
    @PreAuthorize("hasAuthority('USER_DEACTIVATE')")
    public UserResponse status(@AuthenticationPrincipal AuthUser caller, @PathVariable Long id,
                               @Valid @RequestBody UserStatusRequest request) {
        return service.setActive(caller.showroomId(), id, request.active());
    }

    /** The edit form loads the user, so USER_EDIT is enough to read one. */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasAnyAuthority('USER_VIEW', 'USER_EDIT')")
    public UserResponse get(@AuthenticationPrincipal AuthUser caller, @PathVariable Long id) {
        return service.get(caller.showroomId(), id);
    }

    @GetMapping("/users/{id}/vehicles")
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public List<VehicleResponse> vehicles(@AuthenticationPrincipal AuthUser caller, @PathVariable Long id) {
        return service.vehiclesOf(caller.showroomId(), id);
    }
}
