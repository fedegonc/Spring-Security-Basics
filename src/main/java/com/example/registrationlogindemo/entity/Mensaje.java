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
@Table(name = "mensajes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Mensaje {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 1000, name = "contenido", columnDefinition = "TEXT")
    private String contenido;
    
    @Column(nullable = false, name = "fecha_envio")
    private LocalDateTime fecha;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitud_id", nullable = false)
    private Solicitude solicitude;
    
    // Método para establecer la fecha automáticamente al crear un nuevo mensaje
    @PrePersist
    protected void onCreate() {
        fecha = LocalDateTime.now();
    }
    
    // Métodos de conveniencia para compatibilidad con código existente
    public Solicitude getSolicitud() {
        return this.solicitude;
    }
    
    public void setSolicitud(Solicitude solicitud) {
        this.solicitude = solicitud;
    }
    
    public LocalDateTime getFechaEnvio() {
        return this.fecha;
    }
    
    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fecha = fechaEnvio;
    }
}
