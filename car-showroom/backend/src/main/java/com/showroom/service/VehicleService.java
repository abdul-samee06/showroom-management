package com.showroom.service;

import com.showroom.domain.AppUser;
import com.showroom.domain.Vehicle;
import com.showroom.domain.VehicleInvestor;
import com.showroom.dto.Dtos.InvestorInput;
import com.showroom.dto.Dtos.PageResult;
import com.showroom.dto.Dtos.VehicleRequest;
import com.showroom.dto.Dtos.VehicleResponse;
import com.showroom.repo.AppUserRepository;
import com.showroom.repo.VehicleRepository;
import com.showroom.web.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private static final Set<Integer> ALLOWED_WHEELS = Set.of(2, 4, 6);
    private static final Set<String> SORTABLE = Set.of(
            "purchaseDate", "purchasePrice", "modelYear", "modelNo", "numberPlate", "wheels");

    private final VehicleRepository vehicles;
    private final AppUserRepository users;
    private final ShowroomService showrooms;

    @Transactional(readOnly = true)
    public PageResult<VehicleResponse> list(Long showroomId, String q, Integer wheels,
                                            String sortBy, String dir, int page, int size) {
        String field = SORTABLE.contains(sortBy) ? sortBy : "purchaseDate";
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, field).and(Sort.by(Sort.Direction.DESC, "id"));
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), sort);

        Page<Vehicle> result = vehicles.search(showroomId, q == null ? "" : q.trim(), wheels, pageable);
        return new PageResult<>(result.getContent().stream().map(VehicleResponse::from).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public VehicleResponse get(Long showroomId, Long id) {
        return VehicleResponse.from(find(showroomId, id));
    }

    @Transactional
    public VehicleResponse create(Long showroomId, VehicleRequest r) {
        showrooms.require(showroomId);
        Vehicle v = new Vehicle();
        v.setShowroomId(showroomId);
        apply(v, r, showroomId);
        return VehicleResponse.from(vehicles.saveAndFlush(v));
    }

    @Transactional
    public VehicleResponse update(Long showroomId, Long id, VehicleRequest r) {
        Vehicle v = find(showroomId, id);
        apply(v, r, showroomId);
        return VehicleResponse.from(vehicles.saveAndFlush(v));
    }

    @Transactional
    public void delete(Long showroomId, Long id) {
        find(showroomId, id).setDeletedAt(LocalDateTime.now());
    }

    private Vehicle find(Long showroomId, Long id) {
        return vehicles.findByIdAndShowroomIdAndDeletedAtIsNull(id, showroomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vehicle not found."));
    }

    private void apply(Vehicle v, VehicleRequest r, Long showroomId) {
        if (!ALLOWED_WHEELS.contains(r.wheels())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Wheels must be 2, 4 or 6.");
        }
        v.setWheels(r.wheels());
        v.setModelNo(r.modelNo().trim());
        v.setModelYear(r.modelYear());
        v.setNumberPlate(r.numberPlate().trim().toUpperCase());
        v.setPurchasePrice(r.purchasePrice());
        v.setPurchaseDate(r.purchaseDate());
        v.setNotes(ShowroomService.blankToNull(r.notes()));
        syncInvestors(v, r, showroomId);
    }

    /**
     * Updates existing investor rows in place, adds new ones and removes the missing ones.
     * (Replacing the whole list would hit the unique (vehicle, user) key during flush.)
     */
    private void syncInvestors(Vehicle v, VehicleRequest r, Long showroomId) {
        List<InvestorInput> input = r.investors() == null ? List.of() : r.investors();

        Set<Long> seen = new HashSet<>();
        BigDecimal total = BigDecimal.ZERO;
        for (InvestorInput in : input) {
            if (!seen.add(in.userId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "The same investor is listed twice.");
            }
            total = total.add(in.amountInvested());
        }
        if (total.compareTo(r.purchasePrice()) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Total invested (" + total.stripTrailingZeros().toPlainString()
                            + ") is more than the purchase price.");
        }

        Map<Long, VehicleInvestor> existing = v.getInvestors().stream()
                .collect(Collectors.toMap(i -> i.getUser().getId(), Function.identity()));

        for (InvestorInput in : input) {
            VehicleInvestor vi = existing.get(in.userId());
            if (vi == null) {
                AppUser user = users.findByIdAndShowroomIdAndDeletedAtIsNull(in.userId(), showroomId)
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                                "Investor not found in this showroom."));
                vi = new VehicleInvestor();
                vi.setVehicle(v);
                vi.setUser(user);
                v.getInvestors().add(vi);
            }
            vi.setAmountInvested(in.amountInvested());
        }
        v.getInvestors().removeIf(i -> !seen.contains(i.getUser().getId()));
    }
}
