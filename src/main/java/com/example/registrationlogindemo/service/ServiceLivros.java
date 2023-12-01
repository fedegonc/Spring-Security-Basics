package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Livros;

import java.util.List;

public interface ServiceLivros {
    List<Livros> findAll();
    Livros findById(int id);
    Livros save(Livros livros);

    List<Livros> findLivrosByPreco(double preco);

    List<Livros> findLivrosByTituloLike(String titulo);
}