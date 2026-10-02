package com.showroom.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/** Lookup row such as partner / investor. Add new rows to add new user types. */
@Entity
@Table(name = "user_types")
@Getter
@Setter
public class UserType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short id;

    private String code;
    private String label;
}
