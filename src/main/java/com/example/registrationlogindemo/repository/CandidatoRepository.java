package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Candidato;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CandidatoRepository extends JpaRepository<Candidato, Long> {
    List<Candidato> findByEmpleoId(int empleoId);
}
