package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Empleos;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmpleosRepository extends JpaRepository<Empleos, Integer> {
    void findAllById(int id);
    List<Empleos> findEmpleosByDescripcionLike(String descripcion);

    List<Empleos> findEmpleosByPuestoLike(String puesto);

    List<Empleos> findEmpleosByEmpresaLike(String empresa);
}
