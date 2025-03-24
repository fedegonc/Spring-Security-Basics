package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="organizations")
public class Organization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable=false)
    private String name;
    
    @Column(nullable=false)
    private String address;
    
    private String phoneNumber;
    
    private String email;
    
    @Column(length = 1000)
    private String description;
    
    // Logo o imagen de la organización
    private String logo = "default-org-logo.jpg";
    
    // Tipo de organización como enum interno
    public enum OrganizationType {
        CENTRO_ACOPIO("Centro de Acopio"),
        EMPRESA("Empresa"),
        INSTITUCION_RECICLAJE("Institución de Reciclaje");

        private final String displayName;

        OrganizationType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Tipo de organización (Asociación, Cooperativa, etc.)
    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private OrganizationType type;
    
    // Estatus de verificación (pendiente, aprobada, rechazada)
    @Column(nullable=false)
    private String status = "PENDING";
    
    // Fecha de registro
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Documentos de verificación (podrían ser rutas a archivos)
    private String verificationDocuments;
    
    // Relación One-to-Many con User (propietario/administrador)
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;
    
    // Relación Many-to-Many con User (miembros)
    @ManyToMany
    @JoinTable(
        name = "organization_members",
        joinColumns = @JoinColumn(name = "organization_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> members = new ArrayList<>();
    
    // Pre-persist para establecer la fecha de creación
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
