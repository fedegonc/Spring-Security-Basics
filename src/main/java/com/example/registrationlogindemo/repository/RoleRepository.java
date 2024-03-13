package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findByName(String name);
    List<Role> findAll();
}
