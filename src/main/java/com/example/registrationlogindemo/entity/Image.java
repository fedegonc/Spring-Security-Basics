package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.jetbrains.annotations.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String nombre;

    @NotNull
    private String categoria;

    @NotNull
    private boolean activo = true; // Inicializado con true por defecto

    @Lob
    @Column(columnDefinition = "TEXT")
    private String descripcion = ""; // Inicializado a una cadena vacía por defecto

    private String imagen; // Ruta o URL de la imagen

    @NotNull
    private LocalDateTime fecha;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Solicitude.Estado estado = Solicitude.Estado.EN_ESPERA; // Inicializado a EN_ESPERA por defecto

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}