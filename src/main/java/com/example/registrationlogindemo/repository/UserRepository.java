package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

// Interfaz de repositorio para la entidad User que extiende JpaRepository
public interface UserRepository extends JpaRepository<User, Long> {
    // Método para buscar un usuario por su email
    User findByEmail(String email);

    // Método para obtener todos los usuarios
    @SuppressWarnings("null")
    List<User> findAll();

    // Método para buscar un usuario por su ID
    @SuppressWarnings("null")
    Optional<User> findById(Long id);

    // Método para buscar un usuario por su nombre
    User findByName(String name);


    User findByUsername(String username);

    // Método para contar usuarios por rol
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.id = :roleId")
    int countByRolesId(@Param("roleId") Long roleId);

    @Transactional
    @Modifying
    @Query("DELETE FROM User u WHERE u.id = :userId")
    void deleteUserById(@Param("userId") Long userId);
    
    // Método para buscar usuarios por nombre de rol
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

}
