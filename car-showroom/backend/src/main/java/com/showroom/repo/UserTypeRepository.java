package com.showroom.repo;

import com.showroom.domain.UserType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTypeRepository extends JpaRepository<UserType, Short> {
    Optional<UserType> findByCode(String code);
}
