package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;

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


    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String imagen;

    @Column(nullable = false)
    private boolean activo = true; // Inicializar con true por defecto

    @Lob
    private String descripcion = "";


    @Column(nullable = false)
    private String diasDisponibles;

    @Column(nullable = false)
    private String horaRecoleccion;

    @Transient
    private MultipartFile file;

    @Column(nullable = false)
    private LocalDateTime fecha;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private String calle;

    @Column(nullable = false)
    private String barrio;

    @Column(nullable = false)
    private String numeroDeCasa;

    @Column(nullable = false)
    private String referenciaLocal;

    @Column(nullable = false)
    private String telefono;

    @Column(nullable = false)
    private String destino;

    @Column
    private String peso;

    @Column
    private String tamanio;
    
    // Eliminamos la enumeración anidada ya que ahora existe como clase separada

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
