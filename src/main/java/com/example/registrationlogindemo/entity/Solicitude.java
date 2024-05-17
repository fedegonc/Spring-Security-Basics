package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "solicitude")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Solicitude {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String nombre;

    @NotNull
    private String ubicacion;

    @NotNull
    private String categoria;

    @NotNull
    private Boolean activo;

    @Lob
    private String descripcion;

    private String imagen;

    @NotNull
    private String diasDisponibles;

    @NotNull
    private String horaRecoleccion;

    @Transient
    private MultipartFile file;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;
}