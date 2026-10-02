package com.showroom.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Links a user to a vehicle with the amount they invested. */
@Entity
@Table(name = "vehicle_investors")
@Getter
@Setter
public class VehicleInvestor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "amount_invested")
    private BigDecimal amountInvested;

    @Column(name = "invested_on")
    private LocalDate investedOn;
}
