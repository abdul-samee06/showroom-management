package com.showroom.dto;

import com.showroom.domain.Vehicle;
import com.showroom.domain.VehicleInvestor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record VehicleResponse(Long id, Integer wheels, String modelNo, Integer modelYear,
                              String numberPlate, BigDecimal purchasePrice, LocalDate purchaseDate,
                              String notes, List<InvestorResponse> investors) {

    public static VehicleResponse from(Vehicle v) {
        List<InvestorResponse> investors = v.getInvestors().stream()
                .sorted(Comparator.comparing((VehicleInvestor i) -> i.getUser().getName(),
                        String.CASE_INSENSITIVE_ORDER))
                .map(i -> new InvestorResponse(i.getUser().getId(), i.getUser().getName(),
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
