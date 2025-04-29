package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="users")
public class User {

    // Identificador único para el usuario
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Foto de perfil del usuario
    @Column(name = "profile_image", nullable = false)
    private String profileImage = "default-profile.jpg";  // Valor por defecto

    // Nombre de usuario del usuario
    @Column(nullable=false, unique=true)
    private String username;

    // Nombre del usuario
    @Column(nullable=false)
    private String name;

    // Email del usuario, único en la base de datos
    @Column(nullable=false, unique=true)
    private String email;

    // Contraseña del usuario
    @Column(nullable=false)
    private String password;

    // Relación muchos a muchos con la entidad Role, cargada de forma eager y con eliminación en cascada
    @ManyToMany(fetch = FetchType.EAGER, cascade={CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(
            name="users_roles",
            joinColumns={@JoinColumn(name="USER_ID", referencedColumnName="ID")},
            inverseJoinColumns={@JoinColumn(name="ROLE_ID", referencedColumnName="ID")})
    private List<Role> roles = new ArrayList<>();

    // Método para pasar los roles
    public List<String> getUserRoles() {
        List<String> userRoles = new ArrayList<>();
        for (Role role : roles) {
            userRoles.add(role.getName());
        }
        return userRoles;
    }

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade={ CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<Solicitude> solicitudes = new ArrayList<>();
    
    // Fecha de creación del usuario
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Última fecha de actividad del usuario
    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;
    
    // Indica si el usuario está habilitado
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;
    
    // Método que se ejecuta antes de persistir la entidad
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastActiveAt = LocalDateTime.now();
    }
    
    // Método que se ejecuta antes de actualizar la entidad
    @PreUpdate
    protected void onUpdate() {
        lastActiveAt = LocalDateTime.now();
    }
}
