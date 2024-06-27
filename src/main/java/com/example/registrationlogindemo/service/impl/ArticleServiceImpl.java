package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.repository.ImageRepository;
import com.example.registrationlogindemo.service.ArticleService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    public ArticleServiceImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Override
    @Transactional
    public void eliminarEntidad(Long id) {
        articleRepository.deleteById(id);
    }
}
