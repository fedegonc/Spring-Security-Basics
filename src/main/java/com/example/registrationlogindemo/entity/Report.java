package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="reporte")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "title", nullable = true) 
    private String title;

    @Column(nullable = false)
    private String problema;
    
    @Column(nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = true) 
    private ReportStatus status;

    @Column(name = "created_at", nullable = true) 
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Enumeración para los estados del reporte
    public enum ReportStatus {
        PENDING("Pendiente"),
        IN_PROGRESS("En Proceso"),
        RESOLVED("Resuelto"),
        CLOSED("Cerrado");

        private final String displayName;

        ReportStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Método para inicializar valores por defecto antes de persistir
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = ReportStatus.PENDING;
        }
        if (this.title == null || this.title.isEmpty()) {
            this.title = "Reporte #" + this.id;
        }
    }

    // Método para actualizar la fecha de modificación
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
