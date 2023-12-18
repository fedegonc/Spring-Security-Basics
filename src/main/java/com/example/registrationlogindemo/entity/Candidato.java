package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Table(name = "candidato")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Candidato {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String apellido;
    private String email;
    private String linkedin;
    private String aptitudes;
    private String imagen;

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    @ManyToOne
    @JoinColumn(name = "empleo_id")
    private Empleos empleo;

    public void setEmpleo(Empleos empleo) {
        this.empleo = empleo;
    }


}
