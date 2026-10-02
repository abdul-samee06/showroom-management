package com.showroom.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record VehicleRequest(@NotNull Integer wheels,
                             @NotBlank @Size(max = 60) String modelNo,
                             @NotNull @Min(1900) @Max(2100) Integer modelYear,
                             @NotBlank @Size(max = 30) String numberPlate,
                             @NotNull @Positive BigDecimal purchasePrice,
                             @NotNull @PastOrPresent LocalDate purchaseDate,
                             String notes,
                             @Valid List<InvestorRequest> investors) {
}
