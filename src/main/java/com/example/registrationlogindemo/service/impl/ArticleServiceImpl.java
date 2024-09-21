package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.dto.ArticleDto;
import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Article.Categoria; // Asegúrate de importar el enum
import com.example.registrationlogindemo.repository.ArticleRepository;
import com.example.registrationlogindemo.service.ArticleService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    // Convertir Article a ArticleDto
    private ArticleDto convertToDto(Article article) {
        return new ArticleDto(
                article.getId(),
                article.getTitulo(),
                article.getDescripcion(),
                article.getImagenData(), // Cambiado a imagenData
                article.getImagenNombre(), // Cambiado a imagenNombre
                article.getCategoria(),  // Mantener como enum
                article.getFechaRealizado() // Mantener LocalDateTime
        );
    }

    // Listar todos los artículos por categoría
    public List<ArticleDto> getArticlesByCategory(String category) { // Cambiado a Categoria
        List<Article> articles = articleRepository.findByCategoria(category);
        return articles.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Guardar un nuevo artículo
    public void saveArticle(Article article) {
        articleRepository.save(article);
    }

    // Obtener un artículo por su ID
    public ArticleDto getArticleById(Long id) {
        return articleRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado"));
    }

    @Override
    @Transactional
    public void eliminarEntidad(Long id) {
        articleRepository.deleteById(id);
    }
}
