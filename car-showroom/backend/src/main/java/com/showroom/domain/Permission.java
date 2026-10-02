package com.showroom.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** One allowed screen or action, seeded by the system (e.g. VEHICLE_VIEW). */
@Entity
@Table(name = "permissions")
@Getter
@Setter
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    private String code;
    private String label;
    private String module;

    @Column(name = "url_pattern")
    private String urlPattern;
}
