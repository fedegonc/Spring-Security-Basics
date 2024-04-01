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
    // Identificador único para la solicitud
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    // Nombre de la solicitud
    private String nombre;

    // Ubicación de la solicitud
    private String ubicacion;

    // Categoría de la solicitud
    private String categoria;

    // Estado activo o inactivo de la solicitud
    private String activo;

    // Descripción de la solicitud
    @Lob
    private String descripcion;

    // Nombre del archivo de imagen asociado a la solicitud
    private String imagen;

}
