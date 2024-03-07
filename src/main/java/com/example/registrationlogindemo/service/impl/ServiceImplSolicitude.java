package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.repository.solicitudeRepository;
import com.example.registrationlogindemo.service.ServiceSolicitude;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ServiceImplSolicitude implements ServiceSolicitude {

    @Autowired
    solicitudeRepository solicitudeRepository;
    @Override
    public List<Solicitude> findAll() {
        return solicitudeRepository.findAll();
    }

    @Override
    public Solicitude findById(int id) {
        return solicitudeRepository.findById(id).get();
    }

    @Override
    public Solicitude save(Solicitude empleos) {
        return solicitudeRepository.save(empleos);
    }

    @Override
    public List<Solicitude> findSolicitudeByCategoriaLike(String categoria) {
        return solicitudeRepository.findEmpleosByCategoriaLike(categoria);
    }

    @Override
    public List<Solicitude> findSolicitudeByEmpresaLike(String empresa) {
        return solicitudeRepository.findEmpleosByEmpresaLike(empresa);
    }

    @Override
    public List<Solicitude> findSolicitudeActivos() {
        return solicitudeRepository.findEmpleosByActivo("Activo");
    }
}
