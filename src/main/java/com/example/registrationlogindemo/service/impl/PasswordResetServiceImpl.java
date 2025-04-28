package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.PasswordResetCode;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.repository.PasswordResetCodeRepository;
import com.example.registrationlogindemo.repository.UserRepository;
import com.example.registrationlogindemo.service.EmailService;
import com.example.registrationlogindemo.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private PasswordResetCodeRepository resetCodeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Tiempo de expiración del código en minutos
    private static final int EXPIRATION_MINUTES = 15;
    
    @Override
    public String generateResetCode(String email) {
        // Verificar si el usuario existe
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return null; // No revelar si el email existe o no por seguridad
        }
        
        // Generar código de 4 dígitos
        String code = generateRandomCode();
        
        // Invalidar códigos anteriores
        Optional<PasswordResetCode> existingCode = resetCodeRepository.findFirstByEmailAndUsedFalseOrderByExpiryDateDesc(email);
        existingCode.ifPresent(resetCode -> {
            resetCode.setUsed(true);
            resetCodeRepository.save(resetCode);
        });
        
        // Crear nuevo código
        PasswordResetCode resetCode = new PasswordResetCode(
            email,
            code,
            LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES)
        );
        resetCodeRepository.save(resetCode);
        
        // Enviar email con el código
        sendResetCodeEmail(email, code);
        
        return code;
    }
    
    @Override
    public boolean validateResetCode(String email, String code) {
        Optional<PasswordResetCode> resetCodeOpt = resetCodeRepository.findByEmailAndCodeAndUsedFalse(email, code);
        
        if (resetCodeOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetCode resetCode = resetCodeOpt.get();
        
        // Verificar si el código ha expirado
        if (resetCode.isExpired()) {
            resetCode.setUsed(true);
            resetCodeRepository.save(resetCode);
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean resetPassword(String email, String code, String newPassword) {
        // Validar el código
        if (!validateResetCode(email, code)) {
            return false;
        }
        
        // Buscar el usuario
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }
        
        User user = userOpt.get();
        
        // Actualizar la contraseña
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Marcar el código como usado
        Optional<PasswordResetCode> resetCodeOpt = resetCodeRepository.findByEmailAndCodeAndUsedFalse(email, code);
        resetCodeOpt.ifPresent(resetCode -> {
            resetCode.setUsed(true);
            resetCodeRepository.save(resetCode);
        });
        
        return true;
    }
    
    // Método para generar un código aleatorio de 4 dígitos
    private String generateRandomCode() {
        Random random = new Random();
        int code = 1000 + random.nextInt(9000); // Genera un número entre 1000 y 9999
        return String.valueOf(code);
    }
    
    // Método para enviar el email con el código
    private void sendResetCodeEmail(String email, String code) {
        String subject = "Recuperación de contraseña - EcoSolicitud";
        String message = "Hola,\n\n" +
                "Has solicitado restablecer tu contraseña en EcoSolicitud.\n\n" +
                "Tu código de recuperación es: " + code + "\n\n" +
                "Este código expirará en " + EXPIRATION_MINUTES + " minutos.\n\n" +
                "Si no has solicitado restablecer tu contraseña, puedes ignorar este mensaje.\n\n" +
                "Saludos,\n" +
                "Equipo de EcoSolicitud";
        
        emailService.sendSimpleMessage(email, subject, message);
    }
}
