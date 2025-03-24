package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.service.SolicitudeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class SolicitudeServiceImpl implements SolicitudeService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudeServiceImpl.class);

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


    public List<Solicitude> getSolicitudesByUser(User user) {
        return solicitudeRepository.findByUser(user);
    }
    // Método para encontrar solicitudes activas
    @Override
    public List<Solicitude> findSolicitudeActivos() {
        return solicitudeRepository.findSolicitudeByActivo("Activo");
    }
    
    // Implementación del método para obtener solicitudes pendientes
    @Override
    public List<Solicitude> getSolicitudesPendientes() {
        logger.info("Iniciando getSolicitudesPendientes");
        try {
            List<Solicitude> solicitudes = solicitudeRepository.findByEstado("EN_ESPERA");
            logger.info("Solicitudes pendientes encontradas: {}", solicitudes.size());
            return solicitudes;
        } catch (Exception e) {
            logger.error("Error al obtener solicitudes pendientes: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener solicitudes pendientes: " + e.getMessage(), e);
        }
    }
}
