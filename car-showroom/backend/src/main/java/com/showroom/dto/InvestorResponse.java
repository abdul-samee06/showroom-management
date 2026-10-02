package com.showroom.dto;

import java.math.BigDecimal;

public record InvestorResponse(Long userId, String userName, BigDecimal amountInvested,
                               BigDecimal sharePercent) {
}
