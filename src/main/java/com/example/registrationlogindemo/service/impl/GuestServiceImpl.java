package com.example.registrationlogindemo.service.impl;

import com.example.registrationlogindemo.entity.Report;
import com.example.registrationlogindemo.entity.User;
import com.example.registrationlogindemo.service.GuestService;
import com.example.registrationlogindemo.service.NotificationService;
import com.example.registrationlogindemo.service.ReportService;
import com.example.registrationlogindemo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Implementación del servicio para usuarios invitados.
 */
@Service
public class GuestServiceImpl implements GuestService {

    @Autowired
    private ReportService reportService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Override
    public boolean isUserAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication.getPrincipal() instanceof String
                && authentication.getPrincipal().equals("anonymousUser"));
    }
    
    @Override
    public boolean submitReport(@Valid Report report, BindingResult result, RedirectAttributes msg) {
        // Validar el formulario
        if (result.hasErrors()) {
            notificationService.addErrorMessage(msg, "El formulario contiene errores. Por favor, corrige los campos.");
            return false;
        }
        
        // Obtener el usuario autenticado
        Optional<User> authenticatedUserOpt = userService.getAuthenticatedUser();
        if (!authenticatedUserOpt.isPresent()) {
            notificationService.addErrorMessage(msg, "No se pudo encontrar el usuario actual.");
            return false;
        }
        
        // Asignar el usuario al reporte y guardar
        report.setUser(authenticatedUserOpt.get());
        reportService.saveReport(report);
        notificationService.addSuccessMessage(msg, "Reporte realizado con éxito.");
        return true;
    }
}
