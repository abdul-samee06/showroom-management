package com.showroom.service;

import com.showroom.domain.AppUser;
import com.showroom.domain.UserType;
import com.showroom.domain.Vehicle;
import com.showroom.dto.Dtos.UserRequest;
import com.showroom.dto.Dtos.UserResponse;
import com.showroom.dto.Dtos.UserTypeResponse;
import com.showroom.dto.Dtos.VehicleResponse;
import com.showroom.repo.AppUserRepository;
import com.showroom.repo.UserTypeRepository;
import com.showroom.repo.VehicleRepository;
import com.showroom.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final AppUserRepository users;
    private final UserTypeRepository userTypes;
    private final VehicleRepository vehicles;
    private final ShowroomService showrooms;

    @Transactional(readOnly = true)
    public List<UserTypeResponse> types() {
        return userTypes.findAll().stream().map(UserTypeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list(Long showroomId, String typeCode) {
        return users.findByShowroomIdAndDeletedAtIsNullOrderByName(showroomId).stream()
                .filter(u -> typeCode == null || typeCode.isBlank()
                        || u.getUserType().getCode().equalsIgnoreCase(typeCode))
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse create(Long showroomId, UserRequest r) {
        showrooms.require(showroomId);
        AppUser u = new AppUser();
        u.setShowroomId(showroomId);
        apply(u, r);
        return UserResponse.from(users.saveAndFlush(u));
    }

    @Transactional
    public UserResponse update(Long showroomId, Long id, UserRequest r) {
        AppUser u = find(showroomId, id);
        apply(u, r);
        return UserResponse.from(users.saveAndFlush(u));
    }

    @Transactional
    public void delete(Long showroomId, Long id) {
        AppUser u = find(showroomId, id);
        int count = vehicles.findInvestedByUser(showroomId, id).size();
        if (count > 0) {
            throw new ApiException(HttpStatus.CONFLICT, u.getName() + " is invested in " + count
                    + " vehicle(s). Remove those investments first, or set the user to inactive.");
        }
        u.setDeletedAt(LocalDateTime.now());
        u.setActive(false);
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

    private void apply(AppUser u, UserRequest r) {
        UserType type = userTypes.findByCode(r.userTypeCode().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Unknown user type."));
        u.setUserType(type);
        u.setName(r.name().trim());
        u.setEmail(ShowroomService.blankToNull(r.email()));
        u.setPhone(ShowroomService.blankToNull(r.phone()));
        if (r.active() != null) {
            u.setActive(r.active());
        }
    }
}
