package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Livros;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LivrosRepository extends JpaRepository<Livros, Integer> {
    void findAllById(Long id);
    List<Livros> findLivrosByPreco(double preco);

    List<Livros> findLivrosByTituloLike(String titulo);
}
