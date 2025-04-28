package com.example.registrationlogindemo.service;

public interface PasswordResetService {
    
    /**
     * Genera un código de recuperación para el email especificado
     * @param email Email del usuario
     * @return El código generado
     */
    String generateResetCode(String email);
    
    /**
     * Verifica si un código es válido para un email
     * @param email Email del usuario
     * @param code Código de recuperación
     * @return true si el código es válido, false en caso contrario
     */
    boolean validateResetCode(String email, String code);
    
    /**
     * Restablece la contraseña del usuario
     * @param email Email del usuario
     * @param code Código de recuperación
     * @param newPassword Nueva contraseña
     * @return true si la contraseña fue cambiada, false en caso contrario
     */
    boolean resetPassword(String email, String code, String newPassword);
}
