package com.example.registrationlogindemo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "empleos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Empleos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String puesto;

    private String empresa;

    private String ubicacion;

    private String categoria;

    private String contacto;

    private String imagen;

    private Boolean activo;

    @Lob
    private String descripcion;



}
