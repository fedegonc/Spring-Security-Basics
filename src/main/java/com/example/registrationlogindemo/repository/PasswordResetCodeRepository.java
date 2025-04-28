package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.PasswordResetCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    
    // Buscar código activo por email y código
    Optional<PasswordResetCode> findByEmailAndCodeAndUsedFalse(String email, String code);
    
    // Buscar todos los códigos activos para un email
    Optional<PasswordResetCode> findFirstByEmailAndUsedFalseOrderByExpiryDateDesc(String email);
}
