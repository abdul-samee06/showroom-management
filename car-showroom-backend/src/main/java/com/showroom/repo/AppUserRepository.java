package com.showroom.repo;

import com.showroom.domain.AppUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    @EntityGraph(attributePaths = "roles")
    List<AppUser> findByShowroomIdAndDeletedAtIsNullOrderByName(Long showroomId);

    Optional<AppUser> findByIdAndShowroomIdAndDeletedAtIsNull(Long id, Long showroomId);

    @Query("""
            select u from AppUser u
            where u.showroomId = :sid and lower(u.email) = lower(:email) and u.deletedAt is null
            """)
    Optional<AppUser> findForLogin(@Param("sid") Long showroomId, @Param("email") String email);

    /** Active admins (holders of a built-in role) other than the given user. */
    @Query("""
            select count(distinct u) from AppUser u join u.roles r
            where u.showroomId = :sid and u.deletedAt is null and u.active = true
              and r.system = true and u.id <> :uid
            """)
    long countOtherActiveAdmins(@Param("sid") Long showroomId, @Param("uid") Long userId);

    @Query("""
            select distinct u from AppUser u join u.roles r
            where r.id = :rid and u.deletedAt is null order by u.name
            """)
    List<AppUser> findByRole(@Param("rid") Long roleId);
}
