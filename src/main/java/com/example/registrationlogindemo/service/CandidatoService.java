package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Candidato;

import java.util.List;

public interface CandidatoService {
    void guardarCandidato(Candidato candidato);

    List<Candidato> obtenerCandidatosPorEmpleo(int empleoId);

    List<Candidato> findAll();
}
