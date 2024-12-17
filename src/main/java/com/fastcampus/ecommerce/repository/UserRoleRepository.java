package com.fastcampus.ecommerce.repository;

import com.fastcampus.ecommerce.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRole.UserRoleId> {
    void deleteByUserId(Long userId);
}
