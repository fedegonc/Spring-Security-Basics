package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface ArticleService {

    List<Article> findAllArticles();

    Optional<Article> findArticleById(Long id);

    Article createArticle(Article article, User user, MultipartFile file) throws IOException;

    Article updateArticle(Long id, Article article, MultipartFile file) throws IOException;

    void deleteArticle(Long id);

    List<Article> getArticlesByCategory(Article.Categoria categoria);
}
