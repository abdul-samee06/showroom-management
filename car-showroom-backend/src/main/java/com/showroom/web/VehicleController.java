package com.showroom.web;

import com.showroom.dto.InvestorOptionResponse;
import com.showroom.dto.PageResult;
import com.showroom.dto.VehicleRequest;
import com.showroom.dto.VehicleResponse;
import com.showroom.security.AuthUser;
import com.showroom.service.UserService;
import com.showroom.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService service;
    private final UserService users;

    @GetMapping
    @PreAuthorize("hasAuthority('VEHICLE_VIEW')")
    public PageResult<VehicleResponse> list(@AuthenticationPrincipal AuthUser caller,
                                            @RequestParam(required = false) String q,
                                            @RequestParam(required = false) Integer wheels,
                                            @RequestParam(required = false) Integer year,
                                            @RequestParam(required = false) Long investorId,
                                            @RequestParam(defaultValue = "purchaseDate") String sortBy,
                                            @RequestParam(defaultValue = "desc") String dir,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size) {
        return service.list(caller.showroomId(), q, wheels, year, investorId, sortBy, dir, page, size);
    }

    /** Users to pick as investors (vehicle form) or to filter by (vehicle list); no USER_VIEW needed. */
    @GetMapping("/investor-options")
    @PreAuthorize("hasAnyAuthority('VEHICLE_VIEW', 'VEHICLE_CREATE', 'VEHICLE_EDIT')")
    public List<InvestorOptionResponse> investorOptions(@AuthenticationPrincipal AuthUser caller) {
        return users.investorOptions(caller.showroomId());
    }

    /** The edit form loads the vehicle, so VEHICLE_EDIT is enough to read one. */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VEHICLE_VIEW', 'VEHICLE_EDIT')")
    public VehicleResponse get(@AuthenticationPrincipal AuthUser caller, @PathVariable Long id) {
        return service.get(caller.showroomId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('VEHICLE_CREATE')")
    public VehicleResponse create(@AuthenticationPrincipal AuthUser caller,
                                  @Valid @RequestBody VehicleRequest request) {
        return service.create(caller.showroomId(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_EDIT')")
    public VehicleResponse update(@AuthenticationPrincipal AuthUser caller, @PathVariable Long id,
                                  @Valid @RequestBody VehicleRequest request) {
        return service.update(caller.showroomId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('VEHICLE_DELETE')")
    public void delete(@AuthenticationPrincipal AuthUser caller, @PathVariable Long id) {
        service.delete(caller.showroomId(), id);
    }
}
