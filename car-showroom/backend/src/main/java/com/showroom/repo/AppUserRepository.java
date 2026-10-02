package com.showroom.repo;

import com.showroom.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    List<AppUser> findByShowroomIdAndDeletedAtIsNullOrderByName(Long showroomId);

    Optional<AppUser> findByIdAndShowroomIdAndDeletedAtIsNull(Long id, Long showroomId);
}
