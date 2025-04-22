package com.example.registrationlogindemo.repository;

import com.example.registrationlogindemo.entity.Mensaje;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones con la entidad Mensaje
 */
@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {
    
    /**
     * Busca todos los mensajes de una solicitud ordenados por fecha ascendente (más antiguos primero)
     * @param solicitude Solicitud a la que pertenecen los mensajes
     * @return Lista de mensajes ordenados por fecha
     */
    List<Mensaje> findBySolicitudeOrderByFechaAsc(Solicitude solicitude);
    
    /**
     * Busca todos los mensajes enviados por un usuario específico
     * @param user Usuario emisor de los mensajes
     * @return Lista de mensajes enviados por el usuario
     */
    List<Mensaje> findByUser(User user);
    
    /**
     * Busca todos los mensajes de una solicitud enviados por un usuario específico
     * @param solicitude Solicitud a la que pertenecen los mensajes
     * @param user Usuario emisor de los mensajes
     * @return Lista de mensajes que coinciden con los criterios
     */
    List<Mensaje> findBySolicitudeAndUser(Solicitude solicitude, User user);
}
