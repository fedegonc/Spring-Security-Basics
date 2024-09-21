package com.example.registrationlogindemo.dto;

import com.example.registrationlogindemo.entity.Article.Categoria;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDto {
    private int id;
    private String titulo;
    private String descripcion;
    private byte[] imagenData; // Para la imagen
    private String imagenNombre; // Nombre de la imagen
    private Categoria categoria;
    private LocalDateTime fechaRealizado; // Usando LocalDateTime
}
