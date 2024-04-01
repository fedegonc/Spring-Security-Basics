package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Interfaz de repositorio para la entidad User que extiende JpaRepository
public interface UserRepository extends JpaRepository<User, Long> {
    // Método para buscar un usuario por su email
    User findByEmail(String email);

    // Método para obtener todos los usuarios
    List<User> findAll();

    // Método para buscar un usuario por su ID
    Optional<User> findById(Long id);

    // Método para buscar un usuario por su nombre
    User findByName(String name);


}
