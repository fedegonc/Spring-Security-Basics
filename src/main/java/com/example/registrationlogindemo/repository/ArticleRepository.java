package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Article;
import com.example.registrationlogindemo.entity.Image;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findAll();
    Optional<Article> findById(Long id);

    void deleteById(int Id);

    List<Article> findByUser(User usuario);

    List<Article> findByCategoria(String categoria);



}
