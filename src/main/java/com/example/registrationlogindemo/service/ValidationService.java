package com.example.registrationlogindemo.service;

import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Servicio para centralizar la lógica de validación de entidades y formularios.
 * Permite reutilizar reglas de validación comunes en toda la aplicación.
 */
public interface ValidationService {
    
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
    
    /**
     * Valida errores de enlace de formularios y agrega mensajes apropiados
     * @param bindingResult Resultado del enlace de formulario
     * @param attributes Para agregar mensajes de error
     * @return true si no hay errores de enlace, false en caso contrario
     */
    boolean validateBindingResult(BindingResult bindingResult, RedirectAttributes attributes);
}
