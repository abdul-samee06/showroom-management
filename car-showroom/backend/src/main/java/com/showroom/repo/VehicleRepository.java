package com.showroom.repo;

import com.showroom.domain.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByIdAndShowroomIdAndDeletedAtIsNull(Long id, Long showroomId);

    @Query("""
            select v from Vehicle v
            where v.showroomId = :sid and v.deletedAt is null
              and (:wheels is null or v.wheels = :wheels)
              and (:modelYear is null or v.modelYear = :modelYear)
              and (:investorId is null or exists (
                     select 1 from VehicleInvestor i where i.vehicle = v and i.user.id = :investorId))
              and (:q = '' or lower(v.modelNo) like lower(concat('%', :q, '%'))
                           or lower(v.numberPlate) like lower(concat('%', :q, '%')))
            """)
    Page<Vehicle> search(@Param("sid") Long showroomId,
                         @Param("q") String q,
                         @Param("wheels") Integer wheels,
                         @Param("modelYear") Integer modelYear,
                         @Param("investorId") Long investorId,
                         Pageable pageable);

    @Query("""
            select distinct v from Vehicle v join v.investors i
            where v.showroomId = :sid and v.deletedAt is null and i.user.id = :uid
            order by v.purchaseDate desc
            """)
    List<Vehicle> findInvestedByUser(@Param("sid") Long showroomId, @Param("uid") Long userId);
}
