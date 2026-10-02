package com.showroom.repo;

import com.showroom.domain.Showroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShowroomRepository extends JpaRepository<Showroom, Long> {
    boolean existsByIdAndDeletedAtIsNull(Long id);

    Optional<Showroom> findByCodeIgnoreCaseAndDeletedAtIsNull(String code);

    boolean existsByCodeIgnoreCase(String code);
}
