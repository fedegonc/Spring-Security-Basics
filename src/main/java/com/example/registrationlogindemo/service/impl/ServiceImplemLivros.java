package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Livros;
import com.example.registrationlogindemo.repository.LivrosRepository;
import com.example.registrationlogindemo.service.ServiceLivros;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ServiceImplemLivros implements ServiceLivros {

    @Autowired
    LivrosRepository LivrosRepository;

    @Override
    public List<Livros> findAll() {
        // TODO Auto-generated method stub
        return LivrosRepository.findAll();
    }

    @Override
    public Livros findById(int id) {
        // TODO Auto-generated method stub
        return LivrosRepository.findById(id).get();
    }

    @Override
    public Livros save(Livros livros) {
        // TODO Auto-generated method stub
        return LivrosRepository.save(livros);
    }

    @Override
    public List<Livros> findLivrosByPreco(double preco) {
        return LivrosRepository.findLivrosByPreco(preco);
    }

    @Override
    public List<Livros> findLivrosByTituloLike(String titulo) {
        return LivrosRepository.findLivrosByTituloLike(titulo);
    }
}
