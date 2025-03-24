package com.example.registrationlogindemo.entity;

import jakarta.persistence.EnumType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

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

    // Tipo de organización (centro de acopio, empresa, institución de reciclaje)
    @Enumerated(EnumType.STRING)
    @Column(name = "organization_type")
    private Organization.OrganizationType organizationType;

    // Nombre de la organización
    @Column(name = "organization_name")
    private String organizationName;

    // Relación muchos a muchos con la entidad Role, cargada de forma eager y con eliminación en cascada
    @ManyToMany(fetch = FetchType.EAGER, cascade={CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(
            name="users_roles",
            joinColumns={@JoinColumn(name="USER_ID", referencedColumnName="ID")},
            inverseJoinColumns={@JoinColumn(name="ROLE_ID", referencedColumnName="ID")})
    private List<Role> roles = new ArrayList<>();

    // Organizaciones que posee el usuario
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private List<Organization> ownedOrganizations = new ArrayList<>();

    // Organizaciones a las que pertenece el usuario
    @ManyToMany(mappedBy = "members")
    private List<Organization> memberOrganizations = new ArrayList<>();

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

}
