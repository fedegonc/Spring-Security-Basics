package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "`read`", nullable = false)
    private boolean read = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = true)
    private LocalDateTime readAt;
    
    @Column(nullable = false)
    private String type;  // PLATFORM
    
    @Column(nullable = true)
    private Long relatedEntityId;  // ID de la solicitud relacionada
    
    @Column(nullable = true)
    private String relatedEntityType;  // Tipo de entidad relacionada (ej: "SOLICITUDE")
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public void markAsRead() {
        this.read = true;
        this.readAt = LocalDateTime.now();
    }
}
