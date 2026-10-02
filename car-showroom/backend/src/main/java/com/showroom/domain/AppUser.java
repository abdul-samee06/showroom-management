package com.showroom.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** A person belonging to a showroom (partner, investor, ...). */
@Entity
@Table(name = "users")
@Getter
@Setter
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "showroom_id", nullable = false)
    private Long showroomId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_type_id")
    private UserType userType;

    private String name;
    private String email;
    private String phone;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
