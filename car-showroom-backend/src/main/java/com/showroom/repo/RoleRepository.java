package com.showroom.repo;

import com.showroom.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    List<Role> findByShowroomIdAndDeletedAtIsNullOrderByName(Long showroomId);

    Optional<Role> findByIdAndShowroomIdAndDeletedAtIsNull(Long id, Long showroomId);

    List<Role> findByShowroomIdAndIdInAndDeletedAtIsNull(Long showroomId, Collection<Long> ids);

    Optional<Role> findByShowroomIdAndNameIgnoreCaseAndDeletedAtIsNull(Long showroomId, String name);

    /** Rows of {roleId, number of active-or-not, non-deleted users holding it}. */
    @Query("""
            select r.id, count(u) from AppUser u join u.roles r
            where u.showroomId = :sid and u.deletedAt is null
            group by r.id
            """)
    List<Object[]> countUsersPerRole(@Param("sid") Long showroomId);
}
