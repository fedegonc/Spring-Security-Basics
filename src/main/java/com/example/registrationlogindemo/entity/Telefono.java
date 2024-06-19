package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "telefono")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Telefono {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

}
