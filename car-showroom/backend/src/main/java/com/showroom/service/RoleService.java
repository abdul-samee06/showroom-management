package com.showroom.service;

import com.showroom.domain.AppUser;
import com.showroom.domain.Permission;
import com.showroom.domain.Role;
import com.showroom.dto.PermissionResponse;
import com.showroom.dto.RoleDetailResponse;
import com.showroom.dto.RoleOptionResponse;
import com.showroom.dto.RoleRequest;
import com.showroom.dto.RoleResponse;
import com.showroom.dto.RoleUserResponse;
import com.showroom.repo.AppUserRepository;
import com.showroom.repo.PermissionRepository;
import com.showroom.repo.RoleRepository;
import com.showroom.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final AppUserRepository users;

    @Transactional(readOnly = true)
    public List<RoleResponse> list(Long showroomId) {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : roles.countUsersPerRole(showroomId)) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return roles.findByShowroomIdAndDeletedAtIsNullOrderByName(showroomId).stream()
                .map(r -> RoleResponse.from(r, counts.getOrDefault(r.getId(), 0L)))
                .toList();
    }

    /** Names only, for the Roles field of the user form and the role filter. */
    @Transactional(readOnly = true)
    public List<RoleOptionResponse> options(Long showroomId) {
        return roles.findByShowroomIdAndDeletedAtIsNullOrderByName(showroomId).stream()
                .map(RoleOptionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RoleDetailResponse get(Long showroomId, Long id) {
        return detail(find(showroomId, id));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> allPermissions() {
        return permissions.findAllByOrderByModuleAscIdAsc().stream().map(PermissionResponse::from).toList();
    }

    @Transactional
    public RoleDetailResponse create(Long showroomId, RoleRequest r) {
        requireUniqueName(showroomId, r.name(), null);
        Role role = new Role();
        role.setShowroomId(showroomId);
        apply(role, r);
        return detail(roles.saveAndFlush(role));
    }

    @Transactional
    public RoleDetailResponse update(Long showroomId, Long id, RoleRequest r) {
        Role role = find(showroomId, id);
        requireNotSystem(role, "edited");
        requireUniqueName(showroomId, r.name(), id);
        apply(role, r);
        return detail(roles.saveAndFlush(role));
    }

    @Transactional
    public void delete(Long showroomId, Long id) {
        Role role = find(showroomId, id);
        requireNotSystem(role, "deleted");
        int holders = users.findByRole(id).size();
        if (holders > 0) {
            throw new ApiException(HttpStatus.CONFLICT, holders + " user(s) still have the role "
                    + role.getName() + ". Remove it from them first.");
        }
        role.setDeletedAt(LocalDateTime.now());
    }

    private Role find(Long showroomId, Long id) {
        return roles.findByIdAndShowroomIdAndDeletedAtIsNull(id, showroomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role not found."));
    }

    private static void requireNotSystem(Role role, String verb) {
        if (role.isSystem()) {
            throw new ApiException(HttpStatus.CONFLICT, "The built-in Admin role cannot be " + verb + ".");
        }
    }

    private void requireUniqueName(Long showroomId, String name, Long selfId) {
        roles.findByShowroomIdAndNameIgnoreCaseAndDeletedAtIsNull(showroomId, name.trim())
                .filter(other -> !other.getId().equals(selfId))
                .ifPresent(other -> {
                    throw new ApiException(HttpStatus.CONFLICT, "A role with that name already exists.");
                });
    }

    private void apply(Role role, RoleRequest r) {
        role.setName(r.name().trim());
        role.setDescription(ShowroomService.blankToNull(r.description()));
        Set<String> codes = r.permissionCodes() == null ? Set.of() : r.permissionCodes();
        List<Permission> found = codes.isEmpty() ? List.of() : permissions.findByCodeIn(codes);
        if (found.size() != codes.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown permission in the list.");
        }
        role.setPermissions(new HashSet<>(found));
    }

    private RoleDetailResponse detail(Role role) {
        List<String> codes = role.getPermissions().stream().map(Permission::getCode).sorted().toList();
        List<RoleUserResponse> holders = users.findByRole(role.getId()).stream()
                .map((AppUser u) -> new RoleUserResponse(u.getId(), u.getName()))
                .collect(Collectors.toList());
        return new RoleDetailResponse(role.getId(), role.getName(), role.getDescription(),
                role.isSystem(), codes, holders);
    }
}
