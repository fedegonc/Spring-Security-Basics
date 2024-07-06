package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Image;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    List<Image> findAll();
    Optional<Image> findById(Long id);
    @Transactional
    @Modifying
    void deleteById(Long Id);
}
