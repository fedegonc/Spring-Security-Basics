package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Notification;
import com.example.registrationlogindemo.entity.Solicitude;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.NotificationRepository;
import com.example.registrationlogindemo.repository.SolicitudeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class UserNotificationServiceImpl implements UserNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(UserNotificationServiceImpl.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SolicitudeRepository solicitudeRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Notification createNotification(User user, String title, String message, String type) {
        try {
            // Crear la notificación en la base de datos
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType(type);

            // Guardar en la base de datos
            notification = notificationRepository.save(notification);

            logger.info("Notificación creada para el usuario {}: {}", user.getUsername(), title);
            return notification;
        } catch (DataAccessException e) {
            logger.error("Error al crear notificación: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return null;
        } catch (Exception e) {
            logger.error("Error al crear notificación: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return null;
        }
    }

    @Override
    public Notification createEntityNotification(User user, String title, String message, String type, String entityType, Long entityId) {
        try {
            // Crear la notificación en la base de datos
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitle(title);
            notification.setMessage(message);
            notification.setType("PLATFORM"); // Simplificamos a solo notificaciones en plataforma
            notification.setRelatedEntityType(entityType);
            notification.setRelatedEntityId(entityId);

            // Guardar en la base de datos
            notification = notificationRepository.save(notification);

            logger.info("Notificación creada para el usuario {}: {}", user.getUsername(), title);
            return notification;
        } catch (DataAccessException e) {
            logger.error("Error al crear notificación: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return null;
        } catch (Exception e) {
            logger.error("Error al crear notificación: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return null;
        }
    }

    @Override
    public List<Notification> getUserNotifications(User user) {
        try {
            return notificationRepository.findByUserOrderByCreatedAtDesc(user);
        } catch (DataAccessException e) {
            logger.warn("Error al obtener notificaciones del usuario: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Notification> getUnreadNotifications(User user) {
        try {
            return notificationRepository.findByUserAndReadOrderByCreatedAtDesc(user, false);
        } catch (DataAccessException e) {
            logger.warn("Error al obtener notificaciones no leídas: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean markAsRead(Long notificationId) {
        try {
            Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
            if (notificationOpt.isPresent()) {
                Notification notification = notificationOpt.get();
                notification.markAsRead();
                notificationRepository.save(notification);
                return true;
            }
            return false;
        } catch (DataAccessException e) {
            logger.warn("Error al marcar notificación como leída: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        } catch (Exception e) {
            logger.error("Error al marcar notificación como leída: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        }
    }

    @Override
    public long countUnreadNotifications(User user) {
        try {
            return notificationRepository.countUnreadNotifications(user);
        } catch (DataAccessException e) {
            logger.warn("Error al contar notificaciones no leídas: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return 0;
        }
    }

    @Override
    public boolean notifySolicitudeStatusChange(Long solicitudeId, String newStatus) {
        try {
            Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(solicitudeId);
            
            if (solicitudeOpt.isPresent()) {
                Solicitude solicitude = solicitudeOpt.get();
                User user = solicitude.getUser();
                
                String title = "Cambio de estado en solicitud";
                String message = String.format("Tu solicitud '%s' ha cambiado de estado a: %s",
                        solicitude.getTitulo(), newStatus);

                createEntityNotification(user, title, message, "PLATFORM", "SOLICITUDE", solicitudeId);
                return true;
            }
            return false;
        } catch (DataAccessException e) {
            logger.warn("Error al notificar cambio de estado: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        } catch (Exception e) {
            logger.error("Error al notificar cambio de estado: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        }
    }

    @Override
    public boolean notifyNewSolicitudeToOrganization(Long solicitudeId) {
        try {
            Optional<Solicitude> solicitudeOpt = solicitudeRepository.findById(solicitudeId);
            
            if (solicitudeOpt.isPresent()) {
                Solicitude solicitude = solicitudeOpt.get();
                
                // Buscar usuarios con rol ORGANIZATION
                List<User> organizationUsers = new ArrayList<>();
                userRepository.findAll().stream()
                        .filter(user -> user.getRoles().stream()
                                .anyMatch(role -> role.getName().equals("ROLE_ORGANIZATION")))
                        .forEach(organizationUsers::add);
                
                if (organizationUsers.isEmpty()) {
                    logger.warn("No hay usuarios con rol ORGANIZATION para notificar");
                    return false;
                }
                
                String title = "Nueva solicitud recibida";
                String message = String.format("Se ha recibido una nueva solicitud: '%s' de %s",
                        solicitude.getTitulo(), solicitude.getUser().getUsername());
                
                // Notificar a todos los usuarios de organizaciones
                for (User orgUser : organizationUsers) {
                    createEntityNotification(orgUser, title, message, "PLATFORM", "SOLICITUDE", solicitudeId);
                }
                
                return true;
            }
            return false;
        } catch (DataAccessException e) {
            logger.warn("Error al notificar nueva solicitud a organización: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        } catch (Exception e) {
            logger.error("Error al notificar nueva solicitud a organización: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        }
    }

    @Override
    public boolean notifyNewMessage(Long messageId, Long conversationId, Long senderId, Long recipientId) {
        try {
            // Obtener el usuario destinatario
            Optional<User> recipientOpt = userRepository.findById(recipientId);
            Optional<User> senderOpt = userRepository.findById(senderId);

            if (recipientOpt.isPresent() && senderOpt.isPresent()) {
                User recipient = recipientOpt.get();
                User sender = senderOpt.get();

                String title = "Nuevo mensaje";
                String message = String.format("Has recibido un nuevo mensaje de %s",
                        sender.getUsername());

                createEntityNotification(recipient, title, message, "PLATFORM", "MESSAGE", messageId);
                return true;
            }
            return false;
        } catch (DataAccessException e) {
            logger.warn("Error al notificar nuevo mensaje: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        } catch (Exception e) {
            logger.error("Error al notificar nuevo mensaje: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        }
    }

    @Override
    public boolean notifySuspiciousActivity(Long userId, String activityType, String details) {
        try {
            // Buscar usuarios con rol ADMIN
            List<User> adminUsers = new ArrayList<>();
            userRepository.findAll().stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
                    .forEach(adminUsers::add);

            if (adminUsers.isEmpty()) {
                logger.warn("No hay usuarios con rol ADMIN para notificar");
                return false;
            }

            // Obtener información del usuario que realizó la actividad sospechosa
            Optional<User> suspiciousUserOpt = userRepository.findById(userId);
            String username = suspiciousUserOpt.isPresent() ?
                    suspiciousUserOpt.get().getUsername() : "Usuario desconocido";

            String title = "Actividad sospechosa detectada";
            String message = String.format("Tipo: %s - Usuario: %s - Detalles: %s",
                    activityType, username, details);

            // Notificar a todos los administradores
            for (User admin : adminUsers) {
                createNotification(admin, title, message, "PLATFORM");
            }

            return true;
        } catch (DataAccessException e) {
            logger.warn("Error al notificar actividad sospechosa: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        } catch (Exception e) {
            logger.error("Error al notificar actividad sospechosa: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        }
    }

    @Override
    public boolean notifySystemEvent(String eventType, String component, String details) {
        try {
            // Buscar usuarios con rol ADMIN
            List<User> adminUsers = new ArrayList<>();
            userRepository.findAll().stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getName().equals("ROLE_ADMIN")))
                    .forEach(adminUsers::add);

            if (adminUsers.isEmpty()) {
                logger.warn("No hay usuarios con rol ADMIN para notificar");
                return false;
            }

            String title = String.format("Evento del sistema: %s", eventType);
            String message = String.format("Componente: %s - Detalles: %s", component, details);

            // Notificar a todos los administradores
            for (User admin : adminUsers) {
                createNotification(admin, title, message, "PLATFORM");
            }

            return true;
        } catch (DataAccessException e) {
            logger.warn("Error al notificar evento del sistema: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        } catch (Exception e) {
            logger.error("Error al notificar evento del sistema: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            return false;
        }
    }
}
