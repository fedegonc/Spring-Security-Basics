package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Empleos;
import com.example.registrationlogindemo.entity.Livros;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpleosRepository extends JpaRepository<Empleos, Integer> {

}
