package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Empleos;
import com.example.registrationlogindemo.repository.EmpleosRepository;
import com.example.registrationlogindemo.service.ServiceEmpleos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ServiceImplEmpleos implements ServiceEmpleos {

    @Autowired
    EmpleosRepository empleosRepository;
    @Override
    public List<Empleos> findAll() {
        return empleosRepository.findAll();
    }

    @Override
    public Empleos findById(int id) {
        return empleosRepository.findById(id).get();
    }

    @Override
    public Empleos save(Empleos empleos) {
        return empleosRepository.save(empleos);
    }

    @Override
    public List<Empleos> findEmpleosByDescripcion(String descripcion) {
        return empleosRepository.findEmpleosByDescripcionLike(descripcion);
    }

    @Override
    public List<Empleos> findEmpleosByEmpresaLike(String empresa) {
        return empleosRepository.findEmpleosByEmpresaLike(empresa);
    }

    @Override
    public List<Empleos> findEmpleosByPuestoLike(String puesto) {
        return empleosRepository.findEmpleosByPuestoLike(puesto);
    }


}
