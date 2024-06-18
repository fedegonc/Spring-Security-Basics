package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;
@Entity
@Table(name = "solicitude")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Article {
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

    @NotNull
    private String diasDisponibles;

    @NotNull
    private String horaRecoleccion;

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


    @NotNull
    private String calle;

    @NotNull
    private String barrio;

    @NotNull
    private String numeroDeCasa;

    @NotNull
    private String telefono;

    @NotNull
    private String peso;

    @NotNull
    private String volumen;

    @NotNull
    private String destino;

    public enum Estado {
        EN_ESPERA,
        ACEPTADA,
        RECHAZADA,
        EN_REVISION
    }

    @PrePersist
    @PreUpdate
    private void prePersistUpdate() {
        if (Objects.isNull(this.descripcion)) {
            this.descripcion = "";
        }
        if (Objects.isNull(this.fecha)) {
            this.fecha = LocalDateTime.now();
        }
    }
}
