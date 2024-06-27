package com.example.registrationlogindemo.service;

import jakarta.transaction.Transactional;

public interface ImageService {
    @Transactional
    void eliminarEntidad(Long id);
}
