package com.showroom.repo;

import com.showroom.domain.Showroom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShowroomRepository extends JpaRepository<Showroom, Long> {
    List<Showroom> findByDeletedAtIsNullOrderByName();

    boolean existsByIdAndDeletedAtIsNull(Long id);
}
