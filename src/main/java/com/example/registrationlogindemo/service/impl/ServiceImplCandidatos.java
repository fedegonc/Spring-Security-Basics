package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Candidato;
import com.example.registrationlogindemo.repository.CandidatoRepository;
import com.example.registrationlogindemo.service.CandidatoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceImplCandidatos implements CandidatoService {

    @Autowired
    private CandidatoRepository candidatoRepository;

    @Override
    public void guardarCandidato(Candidato candidato) {
        // Agrega la lógica para guardar el candidato en la base de datos
        candidatoRepository.save(candidato);
    }

    @Override
    public List<Candidato> obtenerCandidatosPorEmpleo(int empleoId) {
        // Implementa la lógica para obtener candidatos por el ID del empleo
        return candidatoRepository.findByEmpleoId(empleoId);
    }

    @Override
    public List<Candidato> findAll() {
        return candidatoRepository.findAll();
    }
}
