package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Image;
import com.example.registrationlogindemo.entity.Telefono;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TelefonoRepository extends JpaRepository<Telefono, Integer> {
    List<Telefono> findAll();
    Optional<Telefono> findById(Long id);

    void deleteById(Long Id);
}
