package com.example.registrationlogindemo.config;

import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.UserNotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Asesor global para todos los controladores
 * Agrega atributos comunes a todos los modelos
 */
@ControllerAdvice
public class GlobalControllerAdvice {
    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);
    
    @Autowired
    private UserStatusService userStatusService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserNotificationService userNotificationService;
    
    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
               && !(authentication.getPrincipal() instanceof String);
    }

    @ModelAttribute("userStatus")
    public UserStatusService userStatus() {
        return userStatusService;
    }
    
    /**
     * Agrega el contador de notificaciones no leídas a todas las vistas
     * Maneja de forma segura el caso en que la tabla de notificaciones no exista
     */
    @ModelAttribute
    public void addNotificationCount(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            try {
                User user = userRepository.findByUsername(userDetails.getUsername());
                if (user != null) {
                    long unreadCount = userNotificationService.countUnreadNotifications(user);
                    model.addAttribute("unreadCount", unreadCount);
                }
            } catch (DataAccessException e) {
                // Si hay un error de acceso a datos (tabla no existe), simplemente establecemos el contador a 0
                logger.warn("Error al obtener notificaciones no leídas: {}", e.getMessage());
                model.addAttribute("unreadCount", 0L);
            } catch (Exception e) {
                // Capturar cualquier otra excepción para evitar que falle la aplicación
                logger.error("Error inesperado al obtener notificaciones: {}", e.getMessage(), e);
                model.addAttribute("unreadCount", 0L);
            }
        } else {
            // Usuario no autenticado, no hay notificaciones
            model.addAttribute("unreadCount", 0L);
        }
    }
}
