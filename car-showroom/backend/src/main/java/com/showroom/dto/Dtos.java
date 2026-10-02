package com.showroom.dto;

import com.showroom.domain.AppUser;
import com.showroom.domain.Showroom;
import com.showroom.domain.UserType;
import com.showroom.domain.Vehicle;
import com.showroom.domain.VehicleInvestor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/** All request/response shapes of the API in one place. */
public final class Dtos {
    private Dtos() {
    }

    // ---------- showrooms ----------
    public record ShowroomRequest(@NotBlank @Size(max = 150) String name,
                                  @Size(max = 30) String phone,
                                  @Size(max = 150) String email,
                                  @Size(max = 255) String address) {
    }

    public record ShowroomResponse(Long id, String name) {
        public static ShowroomResponse from(Showroom s) {
            return new ShowroomResponse(s.getId(), s.getName());
        }
    }

    // ---------- users ----------
    public record UserTypeResponse(String code, String label) {
        public static UserTypeResponse from(UserType t) {
            return new UserTypeResponse(t.getCode(), t.getLabel());
        }
    }

    public record UserRequest(@NotBlank @Size(max = 150) String name,
                              @NotBlank String userTypeCode,
                              @Email @Size(max = 150) String email,
                              @Size(max = 30) String phone,
                              Boolean active) {
    }

    public record UserResponse(Long id, String name, String userTypeCode, String userTypeLabel,
                               String email, String phone, boolean active) {
        public static UserResponse from(AppUser u) {
            return new UserResponse(u.getId(), u.getName(), u.getUserType().getCode(),
                    u.getUserType().getLabel(), u.getEmail(), u.getPhone(), u.isActive());
        }
    }

    // ---------- vehicles ----------
    public record InvestorInput(@NotNull Long userId,
                                @NotNull @Positive BigDecimal amountInvested) {
    }

    public record VehicleRequest(@NotNull Integer wheels,
                                 @NotBlank @Size(max = 60) String modelNo,
                                 @NotNull @Min(1900) @Max(2100) Integer modelYear,
                                 @NotBlank @Size(max = 30) String numberPlate,
                                 @NotNull @Positive BigDecimal purchasePrice,
                                 @NotNull @PastOrPresent LocalDate purchaseDate,
                                 String notes,
                                 @Valid List<InvestorInput> investors) {
    }

    public record InvestorView(Long userId, String userName, BigDecimal amountInvested,
                               BigDecimal sharePercent) {
    }

    public record VehicleResponse(Long id, Integer wheels, String modelNo, Integer modelYear,
                                  String numberPlate, BigDecimal purchasePrice, LocalDate purchaseDate,
                                  String notes, List<InvestorView> investors) {

        public static VehicleResponse from(Vehicle v) {
            List<InvestorView> investors = v.getInvestors().stream()
                    .sorted(Comparator.comparing((VehicleInvestor i) -> i.getUser().getName(),
                            String.CASE_INSENSITIVE_ORDER))
                    .map(i -> new InvestorView(i.getUser().getId(), i.getUser().getName(),
                            i.getAmountInvested(), share(i.getAmountInvested(), v.getPurchasePrice())))
                    .toList();
            return new VehicleResponse(v.getId(), v.getWheels(), v.getModelNo(), v.getModelYear(),
                    v.getNumberPlate(), v.getPurchasePrice(), v.getPurchaseDate(), v.getNotes(), investors);
        }

        private static BigDecimal share(BigDecimal amount, BigDecimal price) {
            if (price == null || price.signum() == 0) {
                return BigDecimal.ZERO;
            }
            return amount.multiply(BigDecimal.valueOf(100)).divide(price, 2, RoundingMode.HALF_UP);
        }
    }

    // ---------- shared ----------
    public record PageResult<T>(List<T> items, int page, int size, long totalElements, int totalPages) {
    }

    public record ErrorResponse(String message, java.util.Map<String, String> fieldErrors) {
    }
}
