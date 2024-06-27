package com.example.registrationlogindemo.service;

import jakarta.transaction.Transactional;

public interface ArticleService {

    @Transactional
    void eliminarEntidad(Long id);
}
