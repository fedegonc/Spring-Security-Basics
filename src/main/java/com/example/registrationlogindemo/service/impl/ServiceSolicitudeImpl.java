package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.service.SolicitudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceSolicitudeImpl implements SolicitudeService {

    @Autowired
    SolicitudeRepository solicitudeRepository;

    // Método para encontrar todas las solicitudes
    @Override
    public List<Solicitude> findAll() {
        return solicitudeRepository.findAll();
    }

    // Método para encontrar una solicitud por su ID
    @Override
    public Solicitude findById(int id) {
        return solicitudeRepository.findById(id).get();
    }

    // Método para guardar una solicitud
    @Override
    public Solicitude save(Solicitude solicitud) {
        return solicitudeRepository.save(solicitud);
    }

    // Método para encontrar solicitudes por categoría similar
    @Override
    public List<Solicitude> findSolicitudeByCategoriaLike(String categoria) {
        return solicitudeRepository.findSolicitudeByCategoriaLike(categoria);
    }

    // Método para encontrar solicitudes por empresa similar

    // Método para encontrar solicitudes activas
    @Override
    public List<Solicitude> findSolicitudeActivos() {
        return solicitudeRepository.findSolicitudeByActivo("Activo");
    }
}
