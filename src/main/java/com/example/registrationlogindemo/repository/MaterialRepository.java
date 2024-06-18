package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Material;
import com.example.registrationlogindemo.entity.Solicitude;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaterialRepository extends JpaRepository<Material, Integer> {
    List<Material> findAll();
    Optional<Material> findById(Long id);

    void deleteById(Long Id);
}
