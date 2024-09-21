package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000) // Ajusta la longitud según tus necesidades
    private String descripcion = "";

    @Basic
    @Column(name = "imagen_data", nullable = false, columnDefinition = "MEDIUMBLOB")
    private byte[] imagenData; // Para almacenar los bytes de la imagen


    @Column(name = "imagen_nombre")
    private String imagenNombre; // Nombre de la imagen

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Definimos un enum para las categorías
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categoria categoria;

    public enum Categoria {
        GENERICO,
        NOTICIA,
        LEGISLACION,
        EDUCACION_AMBIENTAL,
        ALIANZAS,
        TIPOS_DE_MATERIALES
    }

    @Column(nullable = false)
    private LocalDateTime fechaRealizado; // Cambié a LocalDateTime para mayor precisión
}
