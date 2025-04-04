package com.example.registrationlogindemo.service;

import com.example.registrationlogindemo.entity.Report;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Servicio para manejar operaciones relacionadas con usuarios invitados/no autenticados
 * y páginas públicas.
 */
public interface GuestService {
    
    /**
     * Verifica si el usuario está autenticado y determina si debe
     * redirigir a otra página
     * @param authentication La autenticación actual del usuario
     * @return true si el usuario está autenticado, false en caso contrario
     */
    boolean isUserAuthenticated(Authentication authentication);
    
    /**
     * Procesa un reporte de problema enviado por un usuario
     * @param report El reporte a guardar
     * @param result Resultado de la validación del formulario
     * @param msg Atributos de redirección para mensajes
     * @return true si el reporte se guardó correctamente, false en caso contrario
     */
    boolean submitReport(@Valid Report report, BindingResult result, RedirectAttributes msg);
}
