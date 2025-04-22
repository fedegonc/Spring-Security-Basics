package com.example.registrationlogindemo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad que representa un mensaje en el chat de una solicitud
 */
@Entity
@Table(name = "mensaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mensaje {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(nullable = false, length = 1000)
    private String contenido;
    
    @Column(nullable = false)
    private LocalDateTime fecha;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitude_id", nullable = false)
    private Solicitude solicitude;
    
    // Método para establecer la fecha automáticamente al crear un nuevo mensaje
    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }
}
