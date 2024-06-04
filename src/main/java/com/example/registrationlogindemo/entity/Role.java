package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="roles")
public class Role {
    // Identificador único para el rol
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nombre del rol
    @Column(nullable=false, unique=true)
    private String name;

    // Relación muchos a muchos con la entidad User
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    // Método toString para representar el rol como un string legible
    @Override
    public String toString() {
        switch (this.name) {
            case "ROLE_USER":
                return "User";
            case "ROLE_ADMIN":
                return "Admin";
            case "ROLE_ROOT":
                return "Root";
            default:
                return this.name;
        }
    }
}
