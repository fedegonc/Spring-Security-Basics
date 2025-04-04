package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.ValidationAndNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Implementación del servicio unificado para operaciones de validación y notificación.
 * Combina la funcionalidad de ValidationService y NotificationService para eliminar
 * dependencias circulares y mejorar la cohesión de la aplicación.
 */
@Service
public class ValidationAndNotificationServiceImpl implements ValidationAndNotificationService {

    // Constantes para claves de mensajes flash
    private static final String SUCCESS_KEY = "success";
    private static final String ERROR_KEY = "error";
    private static final String INFO_KEY = "info";
    private static final String WARNING_KEY = "warning";
    
    @Autowired
    private UserRepository userRepository;
    
    // ======= Métodos de notificación =======
    
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
    
    // ======= Métodos de validación de usuarios =======
    
    /**
     * Valida si un email ya existe en el sistema (para usuarios nuevos)
     * @param email Email a validar
     * @param attributes Para agregar mensajes de error
     * @return true si el email es válido (no existe), false en caso contrario
     */
    @Override
    public boolean validateUniqueEmail(String email, RedirectAttributes attributes) {
        User existingUser = userRepository.findByEmail(email);
        if (existingUser != null) {
            addErrorMessage(attributes, 
                "El email " + email + " ya está en uso. Por favor, utilice otro email.");
            return false;
        }
        return true;
    }
    
    /**
     * Valida si un nombre de usuario ya existe en el sistema (para usuarios nuevos)
     * @param username Nombre de usuario a validar
     * @param attributes Para agregar mensajes de error
     * @return true si el username es válido (no existe), false en caso contrario
     */
    @Override
    public boolean validateUniqueUsername(String username, RedirectAttributes attributes) {
        User existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            addErrorMessage(attributes, 
                "El nombre de usuario " + username + " ya está en uso. Por favor, elija otro.");
            return false;
        }
        return true;
    }
    
    /**
     * Valida la fortaleza de una contraseña
     * @param password Contraseña a validar
     * @param attributes Para agregar mensajes de error
     * @return true si la contraseña es fuerte, false en caso contrario
     */
    @Override
    public boolean validatePasswordStrength(String password, RedirectAttributes attributes) {
        if (password.length() < 8) {
            addErrorMessage(attributes, 
                "La contraseña debe tener al menos 8 caracteres.");
            return false;
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasLetter || !hasDigit) {
            addErrorMessage(attributes, 
                "La contraseña debe contener al menos una letra y un número.");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida que las contraseñas coincidan y sean lo suficientemente seguras
     * @param attributes RedirectAttributes para agregar mensajes flash
     * @param newPassword Nueva contraseña
     * @param confirmPassword Confirmación de la contraseña
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
    
    // ======= Métodos de validación de archivos =======
    
    /**
     * Valida la extensión de un archivo
     * @param file Archivo a validar
     * @param allowedExtensions Extensiones permitidas
     * @param attributes Para agregar mensajes de error
     * @return true si el archivo tiene una extensión permitida, false en caso contrario
     */
    @Override
    public boolean validateFileExtension(MultipartFile file, String[] allowedExtensions, RedirectAttributes attributes) {
        if (file == null || file.isEmpty()) {
            return true; // No hay archivo, por lo que no hay problema de extensión
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            addErrorMessage(attributes, "Nombre de archivo no válido");
            return false;
        }
        
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        for (String extension : allowedExtensions) {
            if (fileExtension.equals(extension.toLowerCase())) {
                return true;
            }
        }
        
        addErrorMessage(attributes, 
            "Tipo de archivo no permitido. Por favor, utilice: " + String.join(", ", allowedExtensions));
        return false;
    }
    
    /**
     * Valida el tamaño de un archivo
     * @param file Archivo a validar
     * @param maxSizeInBytes Tamaño máximo en bytes
     * @param attributes Para agregar mensajes de error
     * @return true si el archivo tiene un tamaño válido, false en caso contrario
     */
    @Override
    public boolean validateFileSize(MultipartFile file, long maxSizeInBytes, RedirectAttributes attributes) {
        if (file == null || file.isEmpty()) {
            return true; // No hay archivo, por lo que no hay problema de tamaño
        }
        
        if (file.getSize() > maxSizeInBytes) {
            double maxSizeMB = maxSizeInBytes / (1024.0 * 1024.0);
            addErrorMessage(attributes, 
                "El archivo es demasiado grande. El tamaño máximo permitido es: " + maxSizeMB + " MB");
            return false;
        }
        
        return true;
    }
    
    // ======= Métodos de validación de formularios =======
    
    /**
     * Valida errores de enlace de formularios y agrega mensajes apropiados
     * @param bindingResult Resultado del enlace de formulario
     * @param attributes Para agregar mensajes de error
     * @return true si no hay errores de enlace, false en caso contrario
     */
    @Override
    public boolean validateBindingResult(BindingResult bindingResult, RedirectAttributes attributes) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("Hay errores en el formulario: ");
            
            bindingResult.getAllErrors().forEach(error -> {
                errorMessage.append(error.getDefaultMessage()).append(". ");
            });
            
            addErrorMessage(attributes, errorMessage.toString());
            return false;
        }
        
        return true;
    }
    
    /**
     * Obtiene la extensión de un archivo
     * @param filename Nombre del archivo
     * @return Extensión del archivo con el punto (ej: ".jpg")
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
