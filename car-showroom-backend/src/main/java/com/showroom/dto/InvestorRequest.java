package com.showroom.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record InvestorRequest(@NotNull Long userId,
                              @NotNull @Positive BigDecimal amountInvested) {
}
