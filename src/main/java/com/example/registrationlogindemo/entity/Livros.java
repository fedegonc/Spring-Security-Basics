package com.example.registrationlogindemo.entity;



import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

    @Setter
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Entity
    @Table(name="livros")

    public class Livros {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NotBlank
        @Column(nullable = false, unique = false)
        private String titulo;

        private String genero;

        private String autor;

        private String preco;
        @Lob
        private String resumo;

        private String imagem;

    }
