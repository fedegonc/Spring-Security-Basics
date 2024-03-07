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


}
