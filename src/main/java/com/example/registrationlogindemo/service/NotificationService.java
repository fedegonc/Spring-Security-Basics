package com.example.registrationlogindemo.service;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Servicio para manejar notificaciones y mensajes flash en la aplicación.
 * Centraliza la gestión de mensajes de éxito, error e información.
 */
public interface NotificationService {
    
    /**
     * Agrega un mensaje de éxito a los atributos flash
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param message Mensaje de éxito
     */
    void addSuccessMessage(RedirectAttributes attributes, String message);
    
    /**
     * Agrega un mensaje de error a los atributos flash
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param message Mensaje de error
     */
    void addErrorMessage(RedirectAttributes attributes, String message);
    
    /**
     * Agrega un mensaje informativo a los atributos flash
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param message Mensaje informativo
     */
    void addInfoMessage(RedirectAttributes attributes, String message);
    
    /**
     * Agrega un mensaje de advertencia a los atributos flash
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param message Mensaje de advertencia
     */
    void addWarningMessage(RedirectAttributes attributes, String message);
    
    /**
     * Agrega un mensaje personalizado con una clave específica a los atributos flash
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param key Clave para el mensaje
     * @param message Contenido del mensaje
     */
    void addCustomMessage(RedirectAttributes attributes, String key, String message);
    
    /**
     * Agrega un mensaje de validación de contraseña
     * @param attributes RedirectAttributes para agregar el mensaje flash
     * @param passwordsMatch Indica si las contraseñas coinciden
     * @return true si la validación tuvo éxito, false en caso contrario
     */
    boolean validatePasswordMatch(RedirectAttributes attributes, 
                                 String newPassword, 
                                 String confirmPassword);
}
