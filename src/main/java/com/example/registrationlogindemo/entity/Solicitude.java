package com.example.registrationlogindemo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    private String nombre;

    private String ubicacion;

    private String categoria;

    private String activo;

    @Lob
    private String descripcion;

    private String imagen;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    // Relación muchos a uno con la entidad User
    // Nombre de la columna que actuará como clave externa en la tabla solicitude
    // Objeto que representa al usuario que crea la solicitud
}
