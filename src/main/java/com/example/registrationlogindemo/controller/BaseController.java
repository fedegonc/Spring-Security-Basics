package com.example.registrationlogindemo.controller;

import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.AuthService;
import com.example.registrationlogindemo.service.BreadcrumbService;
import com.example.registrationlogindemo.service.FileStorageService;
import com.example.registrationlogindemo.service.NotificationService;
import com.example.registrationlogindemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Controlador base abstracto que proporciona funcionalidades comunes para todos los controladores.
 * Siguiendo el principio DRY (Don't Repeat Yourself), esta clase contiene métodos utilizados
 * por múltiples controladores para reducir la duplicación de código.
 */
public abstract class BaseController {

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected FileStorageService fileStorageService;
    
    @Autowired
    protected BreadcrumbService breadcrumbService;
    
    @Autowired
    protected AuthService authService;
    
    @Autowired
    protected NotificationService notificationService;

    /**
     * Obtiene el usuario actualmente autenticado
     * @return Usuario autenticado o null si no hay autenticación
     */
    protected User getCurrentUser() {
        return authService.getCurrentUser();
    }

    /**
     * Maneja la subida de imágenes y devuelve el nombre del archivo
     * @param file Archivo a subir
     * @param currentImageName Nombre de imagen actual (si existe)
     * @return Nombre del archivo subido o el nombre actual si no se sube ninguno
     * @throws IOException Si ocurre un error al procesar el archivo
     */
    protected String handleImageUpload(MultipartFile file, String currentImageName) throws IOException {
        return fileStorageService.storeImage(file, currentImageName);
    }

    /**
     * Crea una lista de elementos para el breadcrumb
     * @param baseUrl URL base para los elementos del breadcrumb (ej: "/admin", "/user", "/org")
     * @param homeName Nombre de la página principal (ej: "Dashboard", "Inicio")
     * @param items Elementos adicionales del breadcrumb (texto)
     * @return Lista de mapas con texto y url para cada elemento
     */
    protected List<Map<String, String>> createBreadcrumbs(String baseUrl, String homeName, String... items) {
        return breadcrumbService.createBreadcrumbs(baseUrl, homeName, items);
    }

    /**
     * Método para cambiar la contraseña del usuario
     * @param currentPassword Contraseña actual
     * @param newPassword Nueva contraseña
     * @param confirmPassword Confirmación de la nueva contraseña
     * @param attributes RedirectAttributes para mensajes flash
     * @param redirectUrl URL de redirección después de cambiar la contraseña
     * @return URL de redirección
     */
    protected String changePassword(String currentPassword, String newPassword, 
                                  String confirmPassword, RedirectAttributes attributes, 
                                  String redirectUrl) {
        // Verificar que las contraseñas nuevas coincidan
        if (!notificationService.validatePasswordMatch(attributes, newPassword, confirmPassword)) {
            return "redirect:" + redirectUrl;
        }
        
        // Obtener el usuario autenticado
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            notificationService.addErrorMessage(attributes, "Usuario no encontrado");
            return "redirect:/login";
        }
        
        // Cambiar la contraseña usando el servicio de usuario
        if (getUserService().changePassword(currentUser, currentPassword, newPassword)) {
            notificationService.addSuccessMessage(attributes, "La contraseña se ha actualizado correctamente");
        } else {
            notificationService.addErrorMessage(attributes, "La contraseña actual es incorrecta");
        }
        
        return "redirect:" + redirectUrl;
    }
    
    /**
     * Método abstracto que debe ser implementado por las subclases para proporcionar
     * el servicio de usuario apropiado.
     * @return Servicio de usuario
     */
    protected abstract UserService getUserService();
}
