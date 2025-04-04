package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.NotificationService;
import com.example.registrationlogindemo.service.ValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Implementación del servicio para centralizar la lógica de validación de entidades y formularios.
 * Permite reutilizar reglas de validación comunes en toda la aplicación.
 */
@Service
public class ValidationServiceImpl implements ValidationService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationService notificationService;
    
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
            notificationService.addErrorMessage(attributes, 
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
            notificationService.addErrorMessage(attributes, 
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
            notificationService.addErrorMessage(attributes, 
                "La contraseña debe tener al menos 8 caracteres.");
            return false;
        }
        
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasLetter || !hasDigit) {
            notificationService.addErrorMessage(attributes, 
                "La contraseña debe contener al menos una letra y un número.");
            return false;
        }
        
        return true;
    }
    
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
            notificationService.addErrorMessage(attributes, "Nombre de archivo no válido");
            return false;
        }
        
        String fileExtension = getFileExtension(originalFilename).toLowerCase();
        for (String extension : allowedExtensions) {
            if (fileExtension.equals(extension.toLowerCase())) {
                return true;
            }
        }
        
        notificationService.addErrorMessage(attributes, 
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
            notificationService.addErrorMessage(attributes, 
                "El archivo es demasiado grande. El tamaño máximo permitido es: " + maxSizeMB + " MB");
            return false;
        }
        
        return true;
    }
    
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
            
            notificationService.addErrorMessage(attributes, errorMessage.toString());
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
