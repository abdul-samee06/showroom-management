package com.showroom.service;

import com.showroom.domain.AppUser;
import com.showroom.domain.Role;
import com.showroom.domain.Showroom;
import com.showroom.dto.ShowroomRequest;
import com.showroom.dto.ShowroomResponse;
import com.showroom.repo.AppUserRepository;
import com.showroom.repo.PermissionRepository;
import com.showroom.repo.RoleRepository;
import com.showroom.repo.ShowroomRepository;
import com.showroom.repo.UserTypeRepository;
import com.showroom.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class ShowroomService {
    private final ShowroomRepository showrooms;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final AppUserRepository users;
    private final UserTypeRepository userTypes;
    private final PasswordEncoder encoder;

    /** Creates the showroom, its built-in Admin role (every permission) and the first admin user. */
    @Transactional
    public ShowroomResponse create(ShowroomRequest r) {
        String code = r.code().trim().toUpperCase();
        if (showrooms.existsByCodeIgnoreCase(code)) {
            throw new ApiException(HttpStatus.CONFLICT, "That showroom code is already taken.");
        }
        Showroom s = new Showroom();
        s.setCode(code);
        s.setName(r.name().trim());
        s.setPhone(blankToNull(r.phone()));
        s.setEmail(blankToNull(r.email()));
        s.setAddress(blankToNull(r.address()));
        s = showrooms.save(s);

        Role admin = new Role();
        admin.setShowroomId(s.getId());
        admin.setName("Admin");
        admin.setDescription("Full access to everything");
        admin.setSystem(true);
        admin.setPermissions(new HashSet<>(permissions.findAll()));
        admin = roles.save(admin);

        AppUser first = new AppUser();
        first.setShowroomId(s.getId());
        first.setUserType(userTypes.findByCode("partner")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "User type 'partner' is missing.")));
        first.setName(r.adminName().trim());
        first.setEmail(r.adminEmail().trim());
        first.setPasswordHash(encoder.encode(r.adminPassword()));
        first.getRoles().add(admin);
        users.save(first);

        return ShowroomResponse.from(s);
    }

    /** Throws 404 if the showroom id from the token does not exist (any more). */
    public void require(Long showroomId) {
        if (!showrooms.existsByIdAndDeletedAtIsNull(showroomId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Showroom not found.");
        }
    }

    static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
