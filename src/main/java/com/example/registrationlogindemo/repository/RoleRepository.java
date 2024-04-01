package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Interfaz de repositorio para la entidad Role que extiende JpaRepository
public interface RoleRepository extends JpaRepository<Role, Long> {
    // Método para buscar un rol por su nombre
    Role findByName(String name);

    // Método para obtener todos los roles
    List<Role> findAll();
}
