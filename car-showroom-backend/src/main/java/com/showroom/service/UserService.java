package com.showroom.service;

import com.showroom.domain.AppUser;
import com.showroom.domain.Role;
import com.showroom.domain.UserType;
import com.showroom.domain.Vehicle;
import com.showroom.dto.InvestorOptionResponse;
import com.showroom.dto.UserRequest;
import com.showroom.dto.UserResponse;
import com.showroom.dto.UserTypeResponse;
import com.showroom.dto.VehicleResponse;
import com.showroom.repo.AppUserRepository;
import com.showroom.repo.RoleRepository;
import com.showroom.repo.UserTypeRepository;
import com.showroom.repo.VehicleRepository;
import com.showroom.security.AuthUser;
import com.showroom.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AppUserRepository users;
    private final UserTypeRepository userTypes;
    private final RoleRepository roles;
    private final VehicleRepository vehicles;
    private final ShowroomService showrooms;
    private final PasswordEncoder encoder;

    @Transactional(readOnly = true)
    public List<UserTypeResponse> types() {
        return userTypes.findAll().stream().map(UserTypeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(Long showroomId, String typeCode, Long roleId) {
        return users.findByShowroomIdAndDeletedAtIsNullOrderByName(showroomId).stream()
                .filter(u -> typeCode == null || typeCode.isBlank()
                        || u.getUserType().getCode().equalsIgnoreCase(typeCode))
                .filter(u -> roleId == null || u.getRoles().stream()
                        .anyMatch(r -> r.getId().equals(roleId) && r.getDeletedAt() == null))
                .map(UserResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long showroomId, Long id) {
        return UserResponse.from(find(showroomId, id));
    }

    /** Active and inactive users, as short options for the investor picker and filter. */
    @Transactional(readOnly = true)
    public List<InvestorOptionResponse> investorOptions(Long showroomId) {
        return users.findByShowroomIdAndDeletedAtIsNullOrderByName(showroomId).stream()
                .map(InvestorOptionResponse::from).toList();
    }

    @Transactional
    public UserResponse create(AuthUser caller, UserRequest r) {
        showrooms.require(caller.showroomId());
        AppUser u = new AppUser();
        u.setShowroomId(caller.showroomId());
        applyFields(u, r);
        applyPassword(u, r);
        if (r.roleIds() != null) {
            requireCanAssign(caller);
            u.setRoles(resolveRoles(caller.showroomId(), r.roleIds()));
        } // otherwise the new user has no roles (there is no default role configured)
        return UserResponse.from(users.saveAndFlush(u));
    }

    @Transactional
    public UserResponse update(AuthUser caller, Long id, UserRequest r) {
        AppUser u = find(caller.showroomId(), id);
        applyFields(u, r);
        applyPassword(u, r);

        if (r.active() != null && r.active() != u.isActive()) {
            if (!caller.has("USER_DEACTIVATE")) {
                throw new ApiException(HttpStatus.FORBIDDEN, "You cannot activate or deactivate users.");
            }
            changeActive(caller.showroomId(), u, r.active());
        }
        if (r.roleIds() != null) {
            requireCanAssign(caller);
            Set<Role> wanted = resolveRoles(caller.showroomId(), r.roleIds());
            boolean losesAdmin = holdsAdminRole(u) && wanted.stream().noneMatch(Role::isSystem);
            if (losesAdmin) {
                guardLastAdmin(caller.showroomId(), u);
            }
            u.getRoles().clear();
            u.getRoles().addAll(wanted);
        }
        return UserResponse.from(users.saveAndFlush(u));
    }

    @Transactional
    public UserResponse setActive(Long showroomId, Long id, boolean active) {
        AppUser u = find(showroomId, id);
        changeActive(showroomId, u, active);
        return UserResponse.from(users.saveAndFlush(u));
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> vehiclesOf(Long showroomId, Long id) {
        find(showroomId, id);
        List<Vehicle> list = vehicles.findInvestedByUser(showroomId, id);
        return list.stream().map(VehicleResponse::from).toList();
    }

    private AppUser find(Long showroomId, Long id) {
        return users.findByIdAndShowroomIdAndDeletedAtIsNull(id, showroomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found."));
    }

    private void applyFields(AppUser u, UserRequest r) {
        UserType type = userTypes.findByCode(r.userTypeCode().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Unknown user type."));
        u.setUserType(type);
        u.setName(r.name().trim());
        u.setEmail(ShowroomService.blankToNull(r.email()));
        u.setPhone(ShowroomService.blankToNull(r.phone()));
        if (u.getId() == null && r.active() != null) { // on edit, activation goes through changeActive
            u.setActive(r.active());
        }
    }

    /** A blank password keeps the current one; a user that can log in needs an email. */
    private void applyPassword(AppUser u, UserRequest r) {
        if (r.password() != null && !r.password().isBlank()) {
            if (u.getEmail() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "An email is needed for a user who can log in.");
            }
            u.setPasswordHash(encoder.encode(r.password()));
        }
    }

    private static void requireCanAssign(AuthUser caller) {
        if (!caller.has("USER_ASSIGN_ROLE")) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You cannot change user roles.");
        }
    }

    private Set<Role> resolveRoles(Long showroomId, Set<Long> ids) {
        if (ids.isEmpty()) {
            return new HashSet<>();
        }
        List<Role> found = roles.findByShowroomIdAndIdInAndDeletedAtIsNull(showroomId, ids);
        if (found.size() != ids.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Unknown role in the list.");
        }
        return new HashSet<>(found);
    }

    private void changeActive(Long showroomId, AppUser u, boolean active) {
        if (!active && u.isActive() && holdsAdminRole(u)) {
            guardLastAdmin(showroomId, u);
        }
        u.setActive(active);
    }

    private static boolean holdsAdminRole(AppUser u) {
        return u.getRoles().stream().anyMatch(Role::isSystem);
    }

    private void guardLastAdmin(Long showroomId, AppUser u) {
        if (u.isActive() && users.countOtherActiveAdmins(showroomId, u.getId()) == 0) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This is the last active admin. It cannot be deactivated or lose the Admin role.");
        }
    }
}
