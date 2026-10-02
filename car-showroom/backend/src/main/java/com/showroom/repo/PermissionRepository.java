package com.showroom.repo;

import com.showroom.domain.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Short> {
    List<Permission> findAllByOrderByModuleAscIdAsc();

    List<Permission> findByCodeIn(Collection<String> codes);
}
