package com.showroom.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** A customer of the system. */
@Entity
@Table(name = "showrooms")
@Getter
@Setter
public class Showroom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Typed on the login screen. */
    private String code;

    private String name;
    private String phone;
    private String email;
    private String address;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
