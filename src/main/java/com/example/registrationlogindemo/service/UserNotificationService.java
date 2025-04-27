package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Notification;
import com.example.registrationlogindemo.entity.User;

import java.util.List;

/**
 * Servicio para gestionar las notificaciones de usuarios.
 * Maneja la creación, recuperación y actualización de notificaciones.
 */
public interface UserNotificationService {
    
    /**
     * Crea una notificación para un usuario
     * @param user Usuario destinatario
     * @param title Título de la notificación
     * @param message Mensaje de la notificación
     * @param type Tipo de notificación (EMAIL, PLATFORM, BOTH)
     * @return La notificación creada
     */
    Notification createNotification(User user, String title, String message, String type);
    
    /**
     * Crea una notificación relacionada con una entidad (ej: solicitud)
     * @param user Usuario destinatario
     * @param title Título de la notificación
     * @param message Mensaje de la notificación
     * @param type Tipo de notificación
     * @param entityType Tipo de entidad relacionada
     * @param entityId ID de la entidad relacionada
     * @return La notificación creada
     */
    Notification createEntityNotification(User user, String title, String message, 
                                         String type, String entityType, Integer entityId);
    
    /**
     * Obtiene todas las notificaciones de un usuario
     * @param user Usuario
     * @return Lista de notificaciones
     */
    List<Notification> getUserNotifications(User user);
    
    /**
     * Obtiene las notificaciones no leídas de un usuario
     * @param user Usuario
     * @return Lista de notificaciones no leídas
     */
    List<Notification> getUnreadNotifications(User user);
    
    /**
     * Marca una notificación como leída
     * @param notificationId ID de la notificación
     * @return true si se marcó correctamente
     */
    boolean markAsRead(Long notificationId);
    
    /**
     * Cuenta las notificaciones no leídas de un usuario
     * @param user Usuario
     * @return Número de notificaciones no leídas
     */
    long countUnreadNotifications(User user);
    
    /**
     * Notifica un cambio de estado en una solicitud
     * @param solicitudeId ID de la solicitud
     * @param newStatus Nuevo estado
     * @return true si se envió la notificación correctamente
     */
    boolean notifySolicitudeStatusChange(Integer solicitudeId, String newStatus);
    
    /**
     * Notifica a la organización sobre una nueva solicitud
     * @param solicitudeId ID de la solicitud
     * @return true si se envió la notificación correctamente
     */
    boolean notifyNewSolicitudeToOrganization(Integer solicitudeId);
    
    /**
     * Notifica sobre un nuevo mensaje en una conversación
     * @param messageId ID del mensaje
     * @param conversationId ID de la conversación
     * @param senderId ID del remitente
     * @param recipientId ID del destinatario
     * @return true si se envió la notificación correctamente
     */
    boolean notifyNewMessage(Long messageId, Long conversationId, Long senderId, Long recipientId);
    
    /**
     * Notifica al administrador sobre actividad sospechosa
     * @param userId ID del usuario que realizó la actividad
     * @param activityType Tipo de actividad (LOGIN_FAILED, SPAM_DETECTED, etc.)
     * @param details Detalles adicionales
     * @return true si se envió la notificación correctamente
     */
    boolean notifySuspiciousActivity(Long userId, String activityType, String details);
    
    /**
     * Notifica al administrador sobre eventos importantes del sistema
     * @param eventType Tipo de evento (ERROR, WARNING, INFO)
     * @param component Componente afectado
     * @param details Detalles del evento
     * @return true si se envió la notificación correctamente
     */
    boolean notifySystemEvent(String eventType, String component, String details);
}
