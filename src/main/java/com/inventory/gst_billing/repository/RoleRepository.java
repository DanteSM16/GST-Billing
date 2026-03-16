package com.inventory.gst_billing.repository;


import com.inventory.gst_billing.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(String roleName); // Spring  writes: SELECT * FROM roles WHERE role_name = ?
}