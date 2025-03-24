package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;

import java.util.List;

public interface SolicitudeService {

    // Método para encontrar todas las solicitudes
    List<Solicitude> findAll();

    // Método para encontrar una solicitud por su ID
    Solicitude findById(int id);

    // Método para guardar una solicitud
    Solicitude save(Solicitude solicitude);

    // Método para encontrar solicitudes por categoría similar
    List<Solicitude> findSolicitudeByCategoriaLike(String descripcion);


    // Método para encontrar solicitudes activas
    List<Solicitude> findSolicitudeActivos();

    List<Solicitude> getSolicitudesByUser(User usuario);
    
    // Método para obtener solicitudes pendientes
    List<Solicitude> getSolicitudesPendientes();
}
