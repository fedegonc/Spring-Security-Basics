package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Solicitude;


import java.util.List;

public interface ServiceSolicitude {
    List<Solicitude> findAll();
    Solicitude findById(int id);
    Solicitude save(Solicitude empleos);
    List<Solicitude> findSolicitudeByCategoriaLike(String descripcion);
    List<Solicitude> findSolicitudeByEmpresaLike(String nombre);
    List<Solicitude> findSolicitudeActivos();
}
