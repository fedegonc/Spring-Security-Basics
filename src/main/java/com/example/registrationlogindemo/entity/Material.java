package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="material")
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotNull
    private String nombre;

    @NotNull
    private String categoria;

    @NotNull
    private boolean activo = true; // Inicializar con true por defecto

    @Lob
    private String descripcion = "";

    private String imagen;

    @Transient
    private MultipartFile file;

    @NotNull
    private LocalDateTime fecha;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Solicitude.Estado estado = Solicitude.Estado.EN_ESPERA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
