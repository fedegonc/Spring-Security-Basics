package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Implementación del servicio para manejar notificaciones y mensajes flash en la aplicación.
 * Centraliza la gestión de mensajes de éxito, error e información.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final String SUCCESS_KEY = "success";
    private static final String ERROR_KEY = "error";
    private static final String INFO_KEY = "info";
    private static final String WARNING_KEY = "warning";
    
    /**
     * Agrega un mensaje de éxito a los atributos flash
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param message Mensaje de éxito
     */
    @Override
    public void addSuccessMessage(RedirectAttributes attributes, String message) {
        attributes.addFlashAttribute(SUCCESS_KEY, message);
    }
    
    /**
     * Agrega un mensaje de error a los atributos flash
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param message Mensaje de error
     */
    @Override
    public void addErrorMessage(RedirectAttributes attributes, String message) {
        attributes.addFlashAttribute(ERROR_KEY, message);
    }
    
    /**
     * Agrega un mensaje informativo a los atributos flash
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param message Mensaje informativo
     */
    @Override
    public void addInfoMessage(RedirectAttributes attributes, String message) {
        attributes.addFlashAttribute(INFO_KEY, message);
    }
    
    /**
     * Agrega un mensaje de advertencia a los atributos flash
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param message Mensaje de advertencia
     */
    @Override
    public void addWarningMessage(RedirectAttributes attributes, String message) {
        attributes.addFlashAttribute(WARNING_KEY, message);
    }
    
    /**
     * Agrega un mensaje personalizado con una clave específica a los atributos flash
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param key Clave para el mensaje
     * @param message Contenido del mensaje
     */
    @Override
    public void addCustomMessage(RedirectAttributes attributes, String key, String message) {
        attributes.addFlashAttribute(key, message);
    }
    
    /**
     * Agrega un mensaje de validación de contraseña
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param passwordsMatch Indica si las contraseñas coinciden
     * @return true si la validación tuvo éxito, false en caso contrario
     */
    @Override
    public boolean validatePasswordMatch(RedirectAttributes attributes, 
                                         String newPassword, 
                                         String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            addErrorMessage(attributes, "La nueva contraseña y la confirmación no coinciden");
            return false;
        }
        return true;
    }
}
