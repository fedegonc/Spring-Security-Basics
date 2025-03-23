package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Integer> {
    @SuppressWarnings("null")
    List<Report> findAll();
    Optional<Report> findById(Long id);

    void deleteById(Long Id);
}
