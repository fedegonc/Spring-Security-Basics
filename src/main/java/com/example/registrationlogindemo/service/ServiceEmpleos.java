package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Empleos;


import java.util.List;

public interface ServiceEmpleos {
    List<Empleos> findAll();
    Empleos findById(int id);
    Empleos save(Empleos empleos);
    List<Empleos> findEmpleosByCategoriaLike(String descripcion);
    List<Empleos> findEmpleosByEmpresaLike(String empresa);

    List<Empleos> findEmpleosActivos();
}
