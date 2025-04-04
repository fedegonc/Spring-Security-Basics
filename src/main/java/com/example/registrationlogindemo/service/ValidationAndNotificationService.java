package com.example.registrationlogindemo.service;

import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Servicio unificado que centraliza las operaciones de validación y notificación.
 * Combina la funcionalidad de ValidationService y NotificationService para simplificar
 * la arquitectura de la aplicación, evitar dependencias circulares y mejorar la cohesión.
 */
public interface ValidationAndNotificationService {
    
    // ======= Métodos de notificación =======
    
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
    
    // ======= Métodos de validación de usuarios =======
    
    /**
     * Valida si un email ya existe en el sistema (para usuarios nuevos)
     * @param email Email a validar
     * @param attributes Para agregar mensajes de error
     * @return true si el email es válido (no existe), false en caso contrario
     */
    boolean validateUniqueEmail(String email, RedirectAttributes attributes);
    
    /**
     * Valida si un nombre de usuario ya existe en el sistema (para usuarios nuevos)
     * @param username Nombre de usuario a validar
     * @param attributes Para agregar mensajes de error
     * @return true si el username es válido (no existe), false en caso contrario
     */
    boolean validateUniqueUsername(String username, RedirectAttributes attributes);
    
    /**
     * Valida la fortaleza de una contraseña
     * @param password Contraseña a validar
     * @param attributes Para agregar mensajes de error
     * @return true si la contraseña es fuerte, false en caso contrario
     */
    boolean validatePasswordStrength(String password, RedirectAttributes attributes);
    
    /**
     * Valida que las contraseñas coincidan y sean lo suficientemente seguras
     * @param attributes RedirectAttributes para agregar mensajes flash
     * @param newPassword Nueva contraseña
     * @param confirmPassword Confirmación de la contraseña
     * @return true si la validación tuvo éxito, false en caso contrario
     */
    boolean validatePasswordMatch(RedirectAttributes attributes, 
                                 String newPassword, 
                                 String confirmPassword);
    
    // ======= Métodos de validación de archivos =======
    
    /**
     * Valida la extensión de un archivo
     * @param file Archivo a validar
     * @param allowedExtensions Extensiones permitidas
     * @param attributes Para agregar mensajes de error
     * @return true si el archivo tiene una extensión permitida, false en caso contrario
     */
    boolean validateFileExtension(MultipartFile file, String[] allowedExtensions, RedirectAttributes attributes);
    
    /**
     * Valida el tamaño de un archivo
     * @param file Archivo a validar
     * @param maxSizeInBytes Tamaño máximo en bytes
     * @param attributes Para agregar mensajes de error
     * @return true si el archivo tiene un tamaño válido, false en caso contrario
     */
    boolean validateFileSize(MultipartFile file, long maxSizeInBytes, RedirectAttributes attributes);
    
    // ======= Métodos de validación de formularios =======
    
    /**
     * Valida errores de enlace de formularios y agrega mensajes apropiados
     * @param bindingResult Resultado del enlace de formulario
     * @param attributes Para agregar mensajes de error
     * @return true si no hay errores de enlace, false en caso contrario
     */
    boolean validateBindingResult(BindingResult bindingResult, RedirectAttributes attributes);
}
