package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.dto.TestNotificationRequest;
import com.example.registrationlogindemo.entity.Notification;
import com.example.registrationlogindemo.entity.Role;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.RoleRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/notificaciones")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
    
    @Autowired
    private UserNotificationService userNotificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    /**
     * Muestra todas las notificaciones del usuario
     */
    @GetMapping
    public String showNotifications(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        List<Notification> notifications = new ArrayList<>();
        long unreadCount = 0;
        
        try {
            notifications = userNotificationService.getUserNotifications(user);
            unreadCount = userNotificationService.countUnreadNotifications(user);
        } catch (DataAccessException e) {
            logger.warn("Error al acceder a la tabla de notificaciones: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            // Continuar con lista vacía y contador en 0
        }
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("user", user);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("currentPage", "Notificaciones");
        
        return "pages/all/notifications";
    }
    
    /**
     * Marca una notificación como leída
     */
    @PostMapping("/{id}/marcar-leida")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id, 
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        boolean success = false;
        long unreadCount = 0;
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            success = userNotificationService.markAsRead(id);
            unreadCount = userNotificationService.countUnreadNotifications(user);
        } catch (DataAccessException e) {
            logger.warn("Error al acceder a la tabla de notificaciones: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            // Continuar con success=false y contador en 0
        }
        
        response.put("success", success);
        response.put("unreadCount", unreadCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crea una notificación de prueba
     */
    @PostMapping("/crear-prueba")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createTestNotification(
            @RequestBody TestNotificationRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        // Guard clauses para validar la solicitud
        if (!StringUtils.hasText(request.getTitle())) {
            response.put("success", false);
            response.put("message", "El título es obligatorio");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (!StringUtils.hasText(request.getMessage())) {
            response.put("success", false);
            response.put("message", "El mensaje es obligatorio");
            return ResponseEntity.badRequest().body(response);
        }
        
        if (!StringUtils.hasText(request.getRecipientRole())) {
            response.put("success", false);
            response.put("message", "El rol del destinatario es obligatorio");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Validar que el rol exista
        String roleName = "ROLE_" + request.getRecipientRole();
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            response.put("success", false);
            response.put("message", "El rol especificado no existe");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            // Obtener usuarios con el rol especificado
            List<User> recipients = userRepository.findAll().stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(r -> r.getName().equals(roleName)))
                    .collect(Collectors.toList());
            
            if (recipients.isEmpty()) {
                response.put("success", false);
                response.put("message", "No hay usuarios con el rol especificado");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Crear notificación para cada usuario con el rol especificado
            for (User recipient : recipients) {
                userNotificationService.createNotification(
                        recipient, 
                        request.getTitle(), 
                        request.getMessage(), 
                        "PLATFORM");
            }
            
            response.put("success", true);
            response.put("message", "Notificaciones creadas con éxito para " + recipients.size() + " usuarios");
            
        } catch (Exception e) {
            logger.error("Error al crear notificaciones de prueba: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            response.put("success", false);
            response.put("message", "Error al crear las notificaciones: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Crea una notificación de prueba para el usuario actual
     */
    @PostMapping("/crear-prueba-personal")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPersonalTestNotification(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userRepository.findByUsername(userDetails.getUsername());
            if (user == null) {
                response.put("success", false);
                response.put("message", "Usuario no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Crear una notificación de prueba para el usuario actual
            String title = "Notificación de prueba";
            String message = "Esta es una notificación de prueba generada el " + 
                            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            
            Notification notification = userNotificationService.createNotification(
                    user, 
                    title, 
                    message, 
                    "PLATFORM");
            
            if (notification == null) {
                throw new Exception("No se pudo crear la notificación");
            }
            
            logger.info("Notificación de prueba creada con ID: {}", notification.getId());
            
            response.put("success", true);
            response.put("message", "Notificación de prueba creada con éxito");
            response.put("notificationId", notification.getId());
            
        } catch (Exception e) {
            logger.error("Error al crear notificación de prueba: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            response.put("success", false);
            response.put("message", "Error al crear la notificación: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Obtiene el número de notificaciones no leídas (para actualización en tiempo real)
     */
    @GetMapping("/no-leidas/contador")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername());
        long unreadCount = 0;
        
        try {
            unreadCount = userNotificationService.countUnreadNotifications(user);
        } catch (DataAccessException e) {
            logger.warn("Error al acceder a la tabla de notificaciones: {}", e.getMessage());
            logger.debug("Detalles del error:", e);
            // Continuar con contador en 0
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("unreadCount", unreadCount);
        
        return ResponseEntity.ok(response);
    }
}
